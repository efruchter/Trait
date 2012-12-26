package efruchter.tp.trait.custom.player;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class SetPlayerTrait extends Trait {

	public SetPlayerTrait() {
		super("Set Player", "Sets this object as the \"player\".");
	}

	@Override
	public void onStart(Entity self, Level level) {
		level.setPlayer(self);
	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {

	}

	@Override
	public void onDeath(Entity self, Level level) {
		level.setPlayer(null);
	}

}
