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
	
	public final Gene wiggleChance, wiggleIntensity;
	private final static Random random = new Random();
	private final float maxWiggle;
	
	public WiggleTrait(final float maxWiggle) {
		super("Wiggle", "Causes random wiggles.");
		registerGene(wiggleChance = new Gene("Probability", "The chance of a wiggle occuring.", 0, 1, 0));
		registerGene(wiggleIntensity = new Gene("Intensity", "The intensity of the wiggle.", 0, 1, .5f));
		this.maxWiggle = maxWiggle;
	}
	
	public WiggleTrait() {
		this(1);
	}
	
	@Override
	public void onStart(final Entity self, final Level l) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		if (random.nextFloat() < wiggleChance.getExpression()) {
			self.x += random.nextFloat() * wiggleIntensity.getValue() * (random.nextBoolean() ? maxWiggle : -maxWiggle);
			self.y += random.nextFloat() * wiggleIntensity.getValue() * (random.nextBoolean() ? maxWiggle : -maxWiggle);
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
