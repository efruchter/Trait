package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

public class TravelSimple extends Trait {

	public Gene dx, dy;

	public TravelSimple() {
		super("TravelSimple", "Move in straight line path.");
		registerGene(dx = new Gene("dx", "movement in x firection", -1, 1, 0));
		registerGene(dy = new Gene("dy", "movement in y firection", -1, 1, 0));
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.x += dx.getValue() * delta;
		self.y += dy.getValue() * delta;
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}

}
