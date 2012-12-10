package efruchter.tp.trait.custom;

import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * Link the radius of an entity to a gene.
 * 
 * @author toriscope
 * 
 */
public class NoHealthDeathTrait extends Trait {
	
	public NoHealthDeathTrait() {
		super("Can Die", "Dies when out of health.");
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		if (self.getHealth() < 0) {
			if (self.entityType == EntityType.NONE) {
				System.out.println("SDDD");
			}
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
