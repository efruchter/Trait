package efruchter.tp.trait.custom.player;

import java.awt.event.KeyEvent;



import efruchter.tp.ClientDefaults;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.util.KeyHolder;

/**
 * Govern the movement of an entity with the keyboard.
 * 
 * @author toriscope
 * 
 */
public class KeyboardControlTrait_Movement extends Trait {

	private float vx, vy;
	public final Gene drag, thrust;
	private static final float MOVE_ADJUST = 1000f / 60f;

	public KeyboardControlTrait_Movement() {
		super("Move Control", "Entity movement linked to keyboard inputs.");
		drag = ClientDefaults.server().getExplorationVector().storeGene("player.move.drag", new Gene("Air Drag", "Amount of air drag."), false);
		thrust = ClientDefaults.server().getExplorationVector().storeGene("player.move.thrust",
		        new Gene("Thrust", "Control the acceleration of movement.", 0, .09f, .014f), false);
		vx = vy = 0;
	}

	@Override
	public void onStart(final Entity self, final Level level) {

	}

	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		if (delta == 0)
			return;
		float ax = 0, ay = 0;

		final float a = thrust.getValue();
		
		KeyHolder holder = KeyHolder.get();
		
		if (holder.isPressed(KeyEvent.VK_A) || holder.isPressed(KeyEvent.VK_LEFT))
			ax -= a;
		if (holder.isPressed(KeyEvent.VK_D) || holder.isPressed(KeyEvent.VK_RIGHT))
			ax += a;
		if (holder.isPressed(KeyEvent.VK_W) || holder.isPressed(KeyEvent.VK_UP))
			ay += a;
		if (holder.isPressed(KeyEvent.VK_S) || holder.isPressed(KeyEvent.VK_DOWN))
			ay -= a;

		vx += ax * delta;
		vy += ay * delta;

		final float dscale = MOVE_ADJUST / delta;
		final float x = (1.01f - drag.getValue()) * dscale;
		final float y = (1.01f - drag.getValue()) * dscale;

		if (Math.abs(x) > .00001f)
			vx *= x;
		if (Math.abs(y) > .00001f)
			vy *= y;

		self.x += vx * delta;
		self.y += vy * delta;
	}

	@Override
	public void onDeath(final Entity self, final Level level) {

	}

}
