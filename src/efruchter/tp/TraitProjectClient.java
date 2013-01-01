package efruchter.tp;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.tp.defaults.CollisionLabel;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.networking.Client;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.custom.LoopScreenTrait;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Movement;
import efruchter.tp.trait.custom.player.PlayerRadiusEditTrait;
import efruchter.tp.trait.custom.player.SetPlayerTrait;
import efruchter.tp.trait.generators.LevelGeneratorCore;
import efruchter.tp.util.RenderUtil;

/**
 * LWJGL Trait-based shmup. This class is a bit messy, needs some splitting up.
 * 
 * @author toriscope
 */
public class TraitProjectClient {

	public static final String VERSION = "00.00.00.00";
	private static boolean isLocalServer;
	private static Level level;
	private static long lastFrame;
	private static int fps;
	private static long lastFPS;
    private static long score;

	private static long guiUpdateDelay = 1000;

	public static void start() {

        score = 0;

		versionCheck();

		level = new Level();

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
	}

	public static void update(int delta) {
		level.onUpdate(delta);
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

		//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");

		// Start the game
		TraitProjectClient.start();
	}
}
