package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

public class RadiusEditTrait extends Trait {

	public Gene radius;

	public RadiusEditTrait(float minR, float maxR, float var) {
		super("Radius", "Has a variable radius.");
		registerGene(radius = new Gene("r", "the radius value", minR, maxR, var));
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.radius = radius.getValue();
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}

}
