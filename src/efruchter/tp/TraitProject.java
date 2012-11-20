package efruchter.tp;

import javax.swing.UIManager;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.tp.entities.Level;
import efruchter.tp.gui.TraitViewer;
import efruchter.tp.traits.custom.KeyboardControlTrait;
import efruchter.tp.traits.custom.TravelSimple;
import efruchter.tp.traits.custom.WiggleTrait;

public class TraitProject {

	/** time at last frame */
	long lastFrame;

	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;

	private Level level;

	public void start() {

		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		level = new Level();

		// Give player keyboard control trait
		level.getPlayer().addTrait(new KeyboardControlTrait(), level);
		WiggleTrait w = new WiggleTrait();
		w.wiggleChance.setExpression(0);
		level.getPlayer().addTrait(w, level);
		level.getPlayer().addTrait(new TravelSimple(), level);
		level.getPlayer().name = "Player Ship";

		// Show the traits for the player
		new TraitViewer(level.getPlayer());

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

		while (!Display.isCloseRequested()) {
			int delta = getDelta();

			update(delta);
			renderGL();

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
		System.exit(0);
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
		long time = getTime();
		int delta = (int) (time - lastFrame);
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
			Display.setTitle("FPS: " + fps + " Entities: "
					+ level.getEntities().size());
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

	public void renderGL() {
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		level.renderGL();
	}

	public static void main(String[] argv) {
		TraitProject timerExample = new TraitProject();
		timerExample.start();
	}
}
