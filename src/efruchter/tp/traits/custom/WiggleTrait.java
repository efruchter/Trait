package efruchter.tp.traits.custom;

import java.util.Random;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Gene.GeneFactory;
import efruchter.tp.traits.Trait;

public class WiggleTrait extends Trait {

	public Gene wiggleChance, wiggleIntensity;
	private static Random random = new Random();
	private float maxWiggle;

	public WiggleTrait(float maxWiggle) {
		super("Wiggle", "Causes random wiggles.");
		registerGene(wiggleChance = GeneFactory.makeDefaultGene("%",
				"The chance of a wiggle occuring."));
		registerGene(wiggleIntensity = GeneFactory.makeDefaultGene("Int.",
				"The intensity of the wiggle."));
		this.maxWiggle = maxWiggle;
	}

	public WiggleTrait() {
		this(1);
	}

	@Override
	public void onStart(Entity self, Level l) {

	}

	@Override
	public void onUpdate(Entity self, Level l, long delta) {
		if (random.nextFloat() < wiggleChance.getExpression()) {
			float dx = random.nextFloat() * wiggleIntensity.getExpression();
			float dy = random.nextFloat() * wiggleIntensity.getExpression();
			self.x += dx * (random.nextBoolean() ? maxWiggle : -maxWiggle);
			self.y += dy * (random.nextBoolean() ? maxWiggle : -maxWiggle);
		}
	}

	@Override
	public void onDeath(Entity self, Level l) {
		// TODO Auto-generated method stub

	}

}
