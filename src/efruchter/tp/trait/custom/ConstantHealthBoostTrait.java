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
	public void onStart(Entity self, Level level) {
		self.health = 999999;
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.health = 999999;
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		self.health = 999999;
	}
	
}
