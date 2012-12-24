package efruchter.tp;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
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
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.custom.ConstantHealthBoostTrait;
import efruchter.tp.trait.custom.LoopScreenTrait;
import efruchter.tp.trait.custom.TravelSimple;
import efruchter.tp.trait.custom.enemy.BasicAttackTrait;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Movement;
import efruchter.tp.trait.custom.player.PlayerRadiusEditTrait;

/**
 * LWJGL Trait-based shmup.
 * 
 * @author toriscope
 */
public class TraitProjectClient {

	public static final String VERSION = "pre-release";
	private static boolean isLocalServer;
	private Level level;
	private final CoreFrame viewer;

	private long lastFrame;
	private int fps;
	private long lastFPS;

	public TraitProjectClient() {

		viewer = new CoreFrame(this);

		level = new Level();

		versionCheck();

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
		Entity player = level.getBlankEntity(EntityType.SHIP);
		EntityFactory.buildShip(player, 100, 100, 10,
				CollisionLabel.PLAYER_LABEL, Color.CYAN, 100);
		// Add control traits to player with arrow-keys
		player.addTrait(new KeyboardControlTrait_Movement(Keyboard.KEY_UP,
				Keyboard.KEY_DOWN, Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT));

		player.addTrait(new KeyboardControlTrait_Attack(Keyboard.KEY_SPACE));
		// Radius editing trait
		PlayerRadiusEditTrait rad = new PlayerRadiusEditTrait(3, 20, 10);
		player.addTrait(rad);
		player.name = "Player Ship";
		// Add screen loop trait
		player.addTrait(new LoopScreenTrait());
		player.addTrait(new ConstantHealthBoostTrait());

		// Build enemy 2
		Entity enemy1 = level.getBlankEntity(EntityType.SHIP);
		EntityFactory.buildShip(enemy1, 600, 520, 20,
				CollisionLabel.ENEMY_LABEL, Color.RED, 100);
		enemy1.name = "Enemy 1";
		BehaviorChain m2 = new BehaviorChain("Attack Pattern",
				"Move around and attack.", true);
		m2.addWait(6000);
		m2.addBehavior(new TravelSimple(.4f, .6f), 500);
		m2.addBehavior(new TravelSimple(.3f, .4f), 500);
		BasicAttackTrait b;
		m2.addBehavior(b = new BasicAttackTrait(), 600);
		b.movePlasmid.dy.setExpression(0.40f);
		enemy1.addTrait(m2);
		enemy1.addTrait(new LoopScreenTrait());

		// Build enemy 2
		Entity enemy2 = level.getBlankEntity(EntityType.SHIP);
		EntityFactory.buildShip(enemy2, 400, 500, 20,
				CollisionLabel.ENEMY_LABEL, Color.RED, 100);
		enemy2.name = "Enemy 2";
		BehaviorChain m = new BehaviorChain("Attack Pattern",
				"Move around and attack.", true);
		m.addWait(2000);
		m.addBehavior(new TravelSimple(.8f, .4f), 500);
		m.addBehavior(new TravelSimple(.5f, .6f), 500);
		m.addBehavior(new BasicAttackTrait(), 500);
		enemy2.addTrait(m);
		enemy2.addTrait(new LoopScreenTrait());

		// Build enemy 2
		Entity enemy3 = level.getBlankEntity(EntityType.SHIP);
		EntityFactory.buildShip(enemy3, 300, 500, 20,
				CollisionLabel.ENEMY_LABEL, Color.RED, 100);
		enemy3.name = "Enemy 3";
		BehaviorChain m3 = new BehaviorChain("Attack Pattern",
				"Move around and attack.", true);
		m3.addBehavior(new TravelSimple(-.08f, .4f), 500);
		m3.addBehavior(new TravelSimple(.5f, .6f), 500);
		m3.addBehavior(new BasicAttackTrait(), 500);
		enemy3.addTrait(m3);
		enemy3.addTrait(new LoopScreenTrait());

		viewer.setLevel(level);

		for (int i = 0; i < 200; i++) {
			Entity e = level.getBlankEntity(EntityType.BG);
			EntityFactory.buildBackgroundStar(e);
		}

		this.level = level;

		final String username = PREFERENCES.get("username", null);

		if (username != null) {
			GeneVectorIO.storeVector(
					new SessionInfo(username, "-1", new SimpleDateFormat(
							"yyyy/MM/dd HH:mm:ss").format(Calendar
							.getInstance().getTime())), GeneVectorIO
							.getExplorationVector());
		} else {
			System.err
					.println("No username set, cannot push vector to server.");
		}
	}

	public void update(int delta) {
		level.onUpdate(delta);
		updateFPS(); // update FPS Counter
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
			// Display.setTitle("FPS: " + fps);
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
				JOptionPane
						.showMessageDialog(null,
								"Your client is out-of-date, please download the latest version.");
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
