package efruchter.tp.trait.custom.player;

import org.lwjgl.input.Keyboard;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.util.KeyUtil;

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
		drag = GeneVectorIO.getExplorationVector().storeGene("player.move.drag", new Gene("Air Drag", "Amount of air drag."), false);
		thrust = GeneVectorIO.getExplorationVector().storeGene("player.move.thrust",
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

		if (KeyUtil.isKeyDown(Keyboard.KEY_A) || KeyUtil.isKeyDown(Keyboard.KEY_LEFT))
			ax -= a;
		if (KeyUtil.isKeyDown(Keyboard.KEY_D) || KeyUtil.isKeyDown(Keyboard.KEY_RIGHT))
			ax += a;
		if (KeyUtil.isKeyDown(Keyboard.KEY_W) || KeyUtil.isKeyDown(Keyboard.KEY_UP))
			ay += a;
		if (KeyUtil.isKeyDown(Keyboard.KEY_S) || KeyUtil.isKeyDown(Keyboard.KEY_DOWN))
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
