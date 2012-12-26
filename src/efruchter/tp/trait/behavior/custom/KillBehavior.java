package efruchter.tp.trait.behavior.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.behavior.Behavior;

public class KillBehavior implements Behavior {

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		level.removeEntity(self);
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}

}
