package efruchter.tp.traits.custom;

import org.lwjgl.input.Keyboard;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.entities.Projectile;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

public class KeyboardControlTrait extends Trait {

	private float vx, vy, cd;
	private Gene drag, acceleration, coolDown, spread, wiggleBigness, bullets;

	public KeyboardControlTrait() {
		super("Keyboard Control", "Entity movement linked to keyboard inputs.");
		registerGene(drag = new Gene("Drag", "Control the amount of air drag."));
		registerGene(acceleration = new Gene("Accel.",
				"Control the acceleration of movement.", 0, .09f, .04f));
		registerGene(coolDown = new Gene("Cooldown",
				"The projectile cooldown.", 0, 64, 1000));
		registerGene(spread = new Gene("Spread", "Bullet spread."));
		registerGene(wiggleBigness = new Gene("Wiggleness",
				"Maximum wiggle magnitude."));
		registerGene(bullets = new Gene("Amount",
				"Amount of bullets per salvo.", 0, 100, 1));
		spread.setExpression(0);
		drag.setExpression(.6f);

		vx = vy = 0;
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {

		float ax = 0, ay = 0;

		float a = acceleration.getValue();

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			ax -= a;
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			ax += a;
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			ay += a;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			ay -= a;

		if (cd < coolDown.getValue()) {
			cd += delta;
		}
		if (cd >= coolDown.getValue()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				cd = 0;
				for (int i = 0; i < bullets.getValue(); i++) {
					Projectile p = new Projectile(self.x, self.y, 3);
					p.addTrait(new TimedDeathTrait(1), level);

					WiggleTrait w = new WiggleTrait(
							20 * wiggleBigness.getExpression());
					w.wiggleChance.setExpression(1);
					w.wiggleIntensity.setExpression(1);

					p.addTrait(w, level);

					TravelSimple t = new TravelSimple();

					t.dx.setExpression(.5f + (float) Math.random()
							* (spread.getExpression())
							* (Math.random() < .5 ? -1 : 1) / 2);
					t.dy.setExpression(1);

					p.addTrait(t, level);
					level.addEntity(p);
				}
			}
		}

		vx += ax * delta;
		vy += ay * delta;

		float x = (1 - drag.getValue());
		float y = (1 - drag.getValue());

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
