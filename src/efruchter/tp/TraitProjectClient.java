package efruchter.tp;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.generators.LevelGeneratorCore;
import efruchter.tp.util.RenderUtil;
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
import efruchter.tp.gui.CoreFrame;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.learning.database.Database.SessionInfo;
import efruchter.tp.networking.Client;
import efruchter.tp.trait.custom.LoopScreenTrait;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Movement;
import efruchter.tp.trait.custom.player.PlayerRadiusEditTrait;
import efruchter.tp.trait.custom.player.SetPlayerTrait;

/**
 * LWJGL Trait-based shmup. This class is a bit messy, needs some splitting up.
 * 
 * @author toriscope
 */
public class TraitProjectClient {

	public static final String VERSION = "00.00.00.00";
	private static boolean isLocalServer;
	private Level level;
	private final CoreFrame viewer;

	private long lastFrame;
	private int fps;
	private long lastFPS;

	private long guiUpdateDelay = 1000;

	public TraitProjectClient() {

		viewer = new CoreFrame(this);

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
	public void resetSim() {

		final Level level = new Level();

		// Build Player
		final Entity player = level.getBlankEntity(EntityType.SHIP);
		EntityFactory.buildShip(player, 100, 100, 10, CollisionLabel.PLAYER_LABEL, Color.CYAN, 50f);
		// Add control traits to player with arrow-keys
		player.addTrait(new KeyboardControlTrait_Movement());

		player.addTrait(new KeyboardControlTrait_Attack());
		// Radius editing trait
		PlayerRadiusEditTrait rad = new PlayerRadiusEditTrait(3, 20, 10);
		player.addTrait(rad);
		player.name = "Player Ship";
		// Add screen loop trait
		player.addTrait(new LoopScreenTrait());
		//player.addTrait(new ConstantHealthBoostTrait());
		player.addTrait(new SetPlayerTrait());

        final LevelGeneratorCore chainer;
		level.getBlankEntity(EntityType.GENERATOR).addTrait(chainer = new LevelGeneratorCore());
        level.setGeneratorCore(chainer);

		viewer.setLevel(level);
		viewer.getStatisticsPanel().setInfo(chainer);

		for (int i = 0; i < 200; i++) {
			Entity e = level.getBlankEntity(EntityType.BG);
			EntityFactory.buildBackgroundStar(e);
		}

        level.addRenderBehavior(new Behavior(){
            public void onStart(Entity self, Level level) {}
            public void onUpdate(final Entity self, final Level level, final long delta) {
                RenderUtil.setColor(Color.CYAN);
                final String playerHealth = level.getPlayer() == null ? "XX" : "" + level.getPlayer().getHealth();
                RenderUtil.drawString(new StringBuffer()
                                      .append("wave ").append(level.getGeneratorCore().getWaveCount())
                                      .append("\n").append("\n")
                                      .append("health ").append(playerHealth)
                                      .toString(), 5, 25);
            }
            public void onDeath(Entity self, Level level) {}
        });

		this.level.onDeath();
		this.level = level;

		final String username = PREFERENCES.get("username", null);

		if (username != null) {
			GeneVectorIO.storeVector(
			        new SessionInfo(username, "-1", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())),
			        GeneVectorIO.getExplorationVector());
		} else {
			System.err.println("No username set, cannot push vector to server.");
		}
	}

	public void update(int delta) {
		level.onUpdate(delta);
		updateFPS();
		if ((guiUpdateDelay -= delta) < 0) {
			guiUpdateDelay = 1000;
			if (level.getGeneratorCore() != null) {
				viewer.getStatisticsPanel().setInfo(level.getGeneratorCore());
            }
		}
	}

	/**
	 * Calculate how many milliseconds have passed since last frame.
	 * 
	 * @return milliseconds passed since last frame
	 */
	public int getDelta() {
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
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
            viewer.getStatisticsPanel().setFPS(fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public void initGL() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public void renderGL(long delta) {
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

	public static void main(String[] argv) throws Exception {

		List<String> params = Arrays.asList(argv);

		isLocalServer = params.contains("-l");

		// UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");

		// Start the game
		new TraitProjectClient();
	}
}
