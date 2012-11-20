package efruchter.tp.traits.custom;

import org.lwjgl.input.Keyboard;

import efruchter.tp.entities.Ship;
import efruchter.tp.traits.Trait;
import efruchter.tp.traits.genes.Gene;
import efruchter.tp.traits.genes.Gene.GeneFactory;


public class KeyboardControlTrait extends Trait {

	private float px, py;
	private Gene drag, acceleration;

	public KeyboardControlTrait() {
		super("Keyboard Control", "Entity movement linked to keyboard inputs.");
		registerGene(drag = GeneFactory.makeDefaultGene("Drag",
				"Control the amount of air drag."));
		registerGene(acceleration = GeneFactory.makeDefaultGene("Accel.",
				"Control the acceleration of movement."));
	}

	@Override
	public void onStart(Ship self) {
		px = self.x;
		py = self.y;
	}

	@Override
	public void onUpdate(Ship self, long delta) {

		float ax = 0, ay = 0;

		float a = .04f * acceleration.getExpression();

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			ax -= a;
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			ax += a;
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			ay += a;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			ay -= a;

		// get velocity
		float vx = self.x - px;
		float vy = self.y - py;

		vx *= .07f * (1f - drag.getExpression());
		vy *= .07f * (1f - drag.getExpression());

		float dx = vx * delta + .5f * ax * delta * delta;
		float dy = vy * delta + .5f * ay * delta * delta;

		if (Math.abs(dx) > .00001)
			px = self.x;
		if (Math.abs(dy) > .00001)
			py = self.y;

		self.x += dx;
		self.y += dy;
	}

	@Override
	public void onDeath(Ship self) {

	}

}
