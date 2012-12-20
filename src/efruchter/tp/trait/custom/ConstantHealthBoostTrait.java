package efruchter.tp.trait.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * Gives an entity a constant health that is really high.
 * 
 * @author toriscope
 * 
 */
public class ConstantHealthBoostTrait extends Trait {
	
	public ConstantHealthBoostTrait() {
		super("Constant Health Boost", "Entity gets a constant influx of health.");
		
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		self.health = 100;
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		self.health = 100;
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		self.health = 100;
	}
	
}
