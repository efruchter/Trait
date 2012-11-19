import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.Level;
import efruchter.entities.Behavior;
import efruchter.entities.Ship;

public class TimerExample {

	/** time at last frame */
	long lastFrame;

	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;

	private Level level;

	public void start() {

		level = new Level();

		// Rig controls to player
		level.getPlayer().updateBehaviors.add(new Behavior() {

			@Override
			public void onStart(Ship self) {

			}

			@Override
			public void onUpdate(Ship self, long delta) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
					self.x -= 0.35f * delta;
				if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
					self.x += 0.35f * delta;

				if (Keyboard.isKeyDown(Keyboard.KEY_UP))
					self.y += 0.35f * delta;
				if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
					self.y -= 0.35f * delta;

				// keep quad on the screen
				if (self.x < 0)
					self.x = 0;
				if (self.x > 800)
					self.x = 800;
				if (self.y < 0)
					self.y = 0;
				if (self.y > 600)
					self.y = 600;
			}

			@Override
			public void onDeath(Ship self) {

			}

		});

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
			Display.setTitle("FPS: " + fps);
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
		TimerExample timerExample = new TimerExample();
		timerExample.start();
	}
}
