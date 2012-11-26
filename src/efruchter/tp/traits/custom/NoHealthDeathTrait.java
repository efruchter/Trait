package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Trait;

/**
 * Link the radius of an entity to a gene.
 * 
 * @author toriscope
 * 
 */
public class NoHealthDeathTrait extends Trait {
	
	public NoHealthDeathTrait() {
		super("Die", "Dies when out of health.");
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		if (self.getHealth() < 0) {
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
