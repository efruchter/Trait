package efruchter.tp.traits.custom.player;

import org.lwjgl.input.Keyboard;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

/**
 * Govern the movement of an entity with the keyboard.
 * 
 * @author toriscope
 * 
 */
public class KeyboardControlTrait_Movement extends Trait {
	
	private float vx, vy;
	public Gene drag, acceleration;
	private int upKey, downKey, leftKey, rightKey;
	private static final float MOVE_ADJUST = 1000f / 60f;
	
	public KeyboardControlTrait_Movement(int upKey, int downKey, int leftKey, int rightKey) {
		super("Move Control", "Entity movement linked to keyboard inputs.");
		registerGene(drag = new Gene("Drag", "Control the amount of air drag."));
		registerGene(acceleration = new Gene("Accel.", "Control the acceleration of movement.", 0, .09f, .04f));
		drag.setExpression(.6f);
		this.upKey = upKey;
		this.downKey = downKey;
		this.leftKey = leftKey;
		this.rightKey = rightKey;
		vx = vy = 0;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		if (delta == 0)
			return;
		float ax = 0, ay = 0;
		
		float a = acceleration.getValue();
		
		if (Keyboard.isKeyDown(leftKey))
			ax -= a;
		if (Keyboard.isKeyDown(rightKey))
			ax += a;
		if (Keyboard.isKeyDown(upKey))
			ay += a;
		if (Keyboard.isKeyDown(downKey))
			ay -= a;
		
		vx += ax * delta;
		vy += ay * delta;

		float dscale = MOVE_ADJUST / delta;
		float x = (1.01f - drag.getValue()) * dscale;
		float y = (1.01f - drag.getValue()) * dscale;
		
		if (Math.abs(x) > .00001f)
			vx *= x;
		if (Math.abs(y) > .00001f)
			vy *= y;
		
		self.x += vx * delta;
		self.y += vy * delta;
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}