package efruchter.tp;

import java.awt.Color;

import javax.swing.UIManager;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.gui.TraitViewer;
import efruchter.tp.traits.custom.RadiusEditTrait;
import efruchter.tp.traits.custom.WiggleTrait;
import efruchter.tp.traits.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.traits.custom.player.KeyboardControlTrait_Movement;

/**
 * LWJGL Trait-based shmup.
 * 
 * @author toriscope
 * 
 */
public class TraitProject {
	
	/** time at last frame */
	long lastFrame;
	
	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;
	
	private Level level;
	private TraitViewer viewer;
	
	public void start() {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		viewer = new TraitViewer();
		
		setup();
		
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
	
	/**
	 * Build the level and entities from scratch.
	 */
	private void setup() {
		
		level = new Level();
		
		// Build Player
		Entity player = EntityFactory.buildShip(100, 100, 10, CollisionLabels.PLAYER_LABEL, Color.CYAN, 100);
		// Add control traits to player with arrow-keys
		player.addTrait(new KeyboardControlTrait_Movement(Keyboard.KEY_UP, Keyboard.KEY_DOWN, Keyboard.KEY_LEFT,
				Keyboard.KEY_RIGHT));
		player.addTrait(new KeyboardControlTrait_Attack(Keyboard.KEY_SPACE));
		// Wiggle trait
		WiggleTrait w = new WiggleTrait();
		w.wiggleChance.setExpression(0);
		player.addTrait(w);
		//Radius editing trait
		player.addTrait(new RadiusEditTrait(3, 20, 10));
		player.name = "Player Ship";
		//Add to level
		level.addEntity(player);
		
		//Build enemy 1
		Entity enemy1 = EntityFactory.buildShip(400, 500, 20, CollisionLabels.ENEMY_LABEL, Color.RED, 100);
		level.addEntity(enemy1);
		
		//Build enemy 2
		Entity enemy2 = EntityFactory.buildShip(600, 500, 20, CollisionLabels.ENEMY_LABEL, Color.RED, 100);
		level.addEntity(enemy2);
		
		// Show the traits for the player
		viewer.setEntity(player);
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
			Display.setTitle("'r' to reset. FPS: " + fps + " Entities: " + level.getEntityCount());
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
		if (Keyboard.isKeyDown(Keyboard.KEY_R) && !Keyboard.isRepeatEvent()) {
			setup();
		}
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
		//Start the game
		new TraitProject().start();
	}
}
