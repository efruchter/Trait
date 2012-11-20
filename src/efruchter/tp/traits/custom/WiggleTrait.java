package efruchter.tp.traits.custom;

import java.util.Random;

import efruchter.tp.entities.Ship;
import efruchter.tp.traits.Trait;
import efruchter.tp.traits.genes.Gene;
import efruchter.tp.traits.genes.Gene.GeneFactory;


public class WiggleTrait extends Trait {

	private Gene wiggleChance, wiggleIntensity;
	private static Random random = new Random();

	public WiggleTrait() {
		super("Wiggle", "Causes random wiggles.");
		registerGene(wiggleChance = GeneFactory.makeDefaultGene("%",
				"The chance of a wiggle occuring."));
		registerGene(wiggleIntensity = GeneFactory.makeDefaultGene("Int.",
				"The intensity of the wiggle."));
		wiggleChance.setExpression(0);
	}

	@Override
	public void onStart(Ship self) {
		
	}

	@Override
	public void onUpdate(Ship self, long delta) {
		if (random.nextFloat() < wiggleChance.getExpression()) {
			float dx = random.nextFloat() * wiggleIntensity.getExpression();
			float dy = random.nextFloat() * wiggleIntensity.getExpression();
			self.x += dx * (random.nextBoolean() ? 1 : -1);
			self.y += dy * (random.nextBoolean() ? 1 : -1);
		}
	}

	@Override
	public void onDeath(Ship self) {
		// TODO Auto-generated method stub

	}

}
