package efruchter.tp;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.trait.gene.Gene;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.networking.Client;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.generators.LevelGeneratorCore;
import efruchter.tp.util.KeyUtil;
import efruchter.tp.util.RenderUtil;

/**
 * LWJGL Trait-based shmup. This class is a bit messy, needs some splitting up.
 * 
 * @author toriscope
 */
public class TraitProjectClient {

	public static final String VERSION = "00.00.00.01";
	private static boolean isLocalServer;
	private static Level level;
	private static long lastFrame;
	private static int fps;
	private static long lastFPS;
    private static long score;

    private static String[] playerControlled;

	public static void start() {

        score = 0;

		versionCheck();

		level = new Level();

        fetchPlayerControlled();

		resetSim();

		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
		Display.setTitle("Trait Project");
		
		while (!Display.isCloseRequested()) {
			int delta = getDelta();

			update(delta);
			renderGL(delta);

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
		System.exit(0);
	}

    private static void fetchPlayerControlled() {
        ClientStateManager.setFlowState(FlowState.LOADING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("playerControlled");
                final String s = c.receive();
                if (s.trim().isEmpty())
                    playerControlled = new String[0];
                else
                    playerControlled = s.split(GeneVectorIO.SEPARATOR);
                System.out.println("Successfully read player-controlled gene list from server.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    c.close();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.err.println("Could not fetch player-controlled gene list from server.");
            playerControlled = new String[0];
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
    }

    /**
	 * Build the level and entities from scratch. Update appropriate GUI
	 * components.
	 */
	public static void resetSim() {
	    
		final Level level = new Level();

        final LevelGeneratorCore chainer;
		level.getBlankEntity(EntityType.GENERATOR).addTrait(chainer = new LevelGeneratorCore());
        level.setGeneratorCore(chainer);

		for (int i = 0; i < 200; i++) {
			Entity e = level.getBlankEntity(EntityType.BG);
			EntityFactory.buildBackgroundStar(e);
		}

        level.addRenderBehavior(new Behavior(){
            public void onStart(Entity self, Level level) {}
            public void onUpdate(final Entity self, final Level level, final long delta) {
                RenderUtil.setColor(Color.CYAN);
                final String playerHealth = level.getPlayer() == null ? "XX" : Integer.toString((int) level.getPlayer().getHealth());
                final String score = level.getPlayer() == null ? "XX" : Long.toString(getScore());
                RenderUtil.drawString(new StringBuffer()
                        .append("health ").append(playerHealth)
                        .append("\n").append("\n")
                        .append("score ").append(score)
                        .append("\n").append("\n")
                        .append("wave ").append(level.getGeneratorCore().getWaveCount())
                        .toString(), 5, 45);
            }
            public void onDeath(Entity self, Level level) {}
        });

		level.onDeath();
		TraitProjectClient.level = level;
		
		ClientStateManager.setFlowState(FlowState.FREE);
	}

	public static void update(int delta) {
	    if(ClientStateManager.getFlowState() == FlowState.FREE)
	        ClientStateManager.setFlowState(FlowState.PLAYING);

        KeyUtil.update();

	    if (KeyUtil.isKeyPressed(Keyboard.KEY_ESCAPE)) {
            System.exit(0);
        }

        level.onUpdate(ClientStateManager.isPaused() ? 0 : delta);

	    if (KeyUtil.isKeyPressed(Keyboard.KEY_RETURN))
	        ClientStateManager.togglePauseState();

		updateFPS();
	}

	/**
	 * Calculate how many milliseconds have passed since last frame.
	 * 
	 * @return milliseconds passed since last frame
	 */
	public static int getDelta() {
		final long time = getTime();
		final int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;
	}

	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public static void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public static void initGL() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public static void renderGL(long delta) {
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
             
		level.renderGL(delta);
		
		if (ClientStateManager.isPaused() && ClientStateManager.getFlowState() != FlowState.EDITING) {
		    RenderUtil.setColor(Color.WHITE);
		    GL11.glPushMatrix();
	        {
	            GL11.glTranslatef(Display.getWidth() / 2, Display.getHeight() / 2, 0);
	            RenderUtil.drawString("PAUSED", 5);
	            GL11.glTranslatef(0, -Display.getHeight() / 8, 0);
	            RenderUtil.drawString("Press <ENTER>", 3);
	        }
	        GL11.glPopMatrix();
		}
	}

	public static void versionCheck() {
		final Client c = getClient();

		try {
			c.reconnect();
			c.send("versioncheck" + VERSION);
			boolean sameVersion = Boolean.parseBoolean(c.receive());
			if (!sameVersion) {
				JOptionPane.showMessageDialog(null, "Your client is out-of-date, please download the latest version.");
				System.exit(0);
			} else {
				System.out.println("Client and Server versions match.");
				return;
			}
		} catch (Exception e) {

		} finally {
			try {
				c.close();
			} catch (Exception e) {
			}
		}
		System.err.println("Cannot check server version.");
	}

    public static long getScore() {
        return score;
    }

    public static void setScore(final long newScore) {
        score = newScore;
    }

    public static Gene[] getPlayerControlledGenes() {
        final GeneVector geneVector = GeneVectorIO.getExplorationVector();
        final Gene[] genes = new Gene[playerControlled.length];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = geneVector.getGene(playerControlled[i]);
        }
        return genes;
    }

	public static Client getClient() {
		if (isLocalServer) {
			return new Client();
		} else {
			return new Client("trait.ericfruchter.com", 8000);
		}
	}

	// User data
	public final static Preferences PREFERENCES;
	static {
		PREFERENCES = Preferences.userNodeForPackage(TraitProjectClient.class);
	}

	public static void main(String[] argv) {

		List<String> params = Arrays.asList(argv);

		isLocalServer = params.contains("-l");

	      //Set Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }

		// Start the game
		TraitProjectClient.start();
	}
}
