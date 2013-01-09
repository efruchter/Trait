package efruchter.tp.trait.custom;

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
	public void onStart(final Entity self, final Level level) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		if (self.getHealth() < 0) {
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
