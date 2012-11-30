package efruchter.tp;

import java.awt.Color;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.entity.Level.LevelListener;
import efruchter.tp.gui.LevelViewer;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.custom.ConstantHealthBoostTrait;
import efruchter.tp.trait.custom.LoopScreenTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TravelSimple;
import efruchter.tp.trait.custom.WiggleTrait;
import efruchter.tp.trait.custom.enemy.BasicAttackTrait;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Movement;

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
	private LevelViewer viewer;
	public boolean markForDeletion = false;
	
	public void start() {
		
		viewer = new LevelViewer(level = new Level());
		
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
			renderGL(delta);
			
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
		//Radius editing trait
		RadiusEditTrait rad = new RadiusEditTrait(3, 20, 10);
		player.addTrait(rad);
		player.name = "Player Ship";
		//Add screen loop trait
		player.addTrait(new LoopScreenTrait());
		player.addTrait(new ConstantHealthBoostTrait());
		//Add to level
		level.addEntity(player);
		
		//Build enemy 2
		Entity enemy1 = EntityFactory.buildShip(600, 500, 20, CollisionLabels.ENEMY_LABEL, Color.RED, 100);
		enemy1.name = "Enemy 1";
		BehaviorChain m2 = new BehaviorChain("Attack Pattern", "Move around and attack.", true);
		m2.addWait(6000);
		m2.addBehavior(new TravelSimple(.4f, .6f), 500);
		m2.addBehavior(new TravelSimple(.3f, .4f), 500);
		BasicAttackTrait b;
		m2.addBehavior(b = new BasicAttackTrait(), 600);
		b.movePlasmid.dy.setExpression(0.40f);
		enemy1.addTrait(m2);
		enemy1.addTrait(new LoopScreenTrait());
		level.addEntity(enemy1);
		
		//Build enemy 2
		Entity enemy2 = EntityFactory.buildShip(400, 500, 20, CollisionLabels.ENEMY_LABEL, Color.RED, 100);
		enemy2.name = "Enemy 2";
		BehaviorChain m = new BehaviorChain("Attack Pattern", "Move around and attack.", true);
		m.addWait(2000);
		m.addBehavior(new TravelSimple(.8f, .4f), 500);
		m.addBehavior(new TravelSimple(.5f, .6f), 500);
		m.addBehavior(new BasicAttackTrait(), 500);
		enemy2.addTrait(m);
		enemy2.addTrait(new LoopScreenTrait());
		level.addEntity(enemy2);
		
		//Create a little behavior cycle
		BehaviorChain c = new BehaviorChain("Wiggle Chain", "A behavior loop. Wait, wiggle, then move.", true);
		c.addWait(1000);
		WiggleTrait f = new WiggleTrait();
		f.wiggleChance.setExpression(0);
		f.wiggleIntensity.setExpression(1);
		c.addBehavior(f, 1000);
		c.addBehavior(new TravelSimple(), 200);
		player.addTrait(c);
		
		level.addLevelListener(new LevelListener() {
			public void shipRemoved(Entity ship) {
				viewer.setLevel(level);
			}
			
			public void shipAdded(Entity ship) {
				viewer.setLevel(level);
			}
		});
		
		viewer.setLevel(level);
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
		//GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	public void renderGL(long delta) {
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		level.renderGL(delta);
	}
	
	public static void main(String[] argv) {
		//Start the game
		new TraitProject().start();
	}
}
