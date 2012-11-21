package efruchter.tp.traits.custom;

import org.lwjgl.input.Keyboard;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.entities.Projectile;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

public class KeyboardControlTrait extends Trait {

	private float px, py, coolD, cd;
	private Gene drag, acceleration, coolDown, spread, wiggleBigness, bullets;

	public KeyboardControlTrait() {
		super("Keyboard Control", "Entity movement linked to keyboard inputs.");
		registerGene(drag = new Gene("Drag", "Control the amount of air drag."));
		registerGene(acceleration = new Gene("Accel.",
				"Control the acceleration of movement."));
		registerGene(coolDown = new Gene("Cooldown", "The projectile cooldown."));
		registerGene(spread = new Gene("Spread", "Bullet spread."));
		registerGene(wiggleBigness = new Gene("Wiggleness",
				"Maximum wiggle magnitude."));
		registerGene(bullets = new Gene("Amount",
				"Amount of bullets per salvo.", 0, 10, 1));
		spread.setExpression(0);
	}

	@Override
	public void onStart(Entity self, Level level) {
		px = self.x;
		py = self.y;
		coolD = 64 * 2;
	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {

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

		if (cd < coolD * coolDown.getExpression()) {
			cd += delta;
		}
		if (cd >= coolD * coolDown.getExpression()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				cd = 0;
				for (int i = 0; i < bullets.getValue(); i++) {
					Projectile p = new Projectile(self.x, self.y, 3);
					p.addTrait(new TimedDeathTrait(1), level);

					WiggleTrait w = new WiggleTrait(20 * wiggleBigness
							.getExpression());
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
	public void onDeath(Entity self, Level level) {

	}

}
