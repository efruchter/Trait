package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Trait;

public class TimedDeathTrait extends Trait {

	private float t, max;

	public TimedDeathTrait(float milliTillDeath) {
		super("TimedDeath", "Die after a certain amount of time");
		max = milliTillDeath;
	}

	public TimedDeathTrait(int secondsTillDeath) {
		this(secondsTillDeath * 1000f);
	}

	@Override
	public void onStart(Entity self, Level level) {
		t = 0;
	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		if ((t = delta + t) >= max) {
			level.removeEntity(self);
		}
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}

}
