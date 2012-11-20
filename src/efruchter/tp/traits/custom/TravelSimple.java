package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Gene.GeneFactory;
import efruchter.tp.traits.Trait;

public class TravelSimple extends Trait {

	public Gene dx, dy;

	public TravelSimple() {
		super("TravelSimple", "Move in straight line path.");
		registerGene(dx = GeneFactory.makeDefaultGene("dx",
				"movement in x firection"));
		registerGene(dy = GeneFactory.makeDefaultGene("dy",
				"movement in y firection"));
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.x += (-1 + 2 * dx.getExpression()) * delta;
		self.y += (-1 + 2 * dy.getExpression()) * delta;
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}

}
