package efruchter.tp.trait.custom;

import java.util.Random;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;

/**
 * Cause random wiggles.
 * 
 * @author toriscope
 * 
 */
public class WiggleTrait extends Trait {
	
	public Gene wiggleChance, wiggleIntensity;
	private static Random random = new Random();
	private float maxWiggle;
	
	public WiggleTrait(float maxWiggle) {
		super("Wiggle", "Causes random wiggles.");
		registerGene(wiggleChance = new Gene("Probability", "The chance of a wiggle occuring.", 0, 1, 0));
		registerGene(wiggleIntensity = new Gene("Intensity", "The intensity of the wiggle.", 0, 1, .5f));
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
			float dx = random.nextFloat() * wiggleIntensity.getValue();
			float dy = random.nextFloat() * wiggleIntensity.getValue();
			self.x += dx * (random.nextBoolean() ? maxWiggle : -maxWiggle);
			self.y += dy * (random.nextBoolean() ? maxWiggle : -maxWiggle);
		}
	}
	
	@Override
	public void onDeath(Entity self, Level l) {
		
	}
	
}
