package efruchter.tp.trait.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * Destroy the entity after a given amount of time
 * 
 * @author toriscope
 * 
 */
public class TimedDeathTrait extends Trait {
	
	private float t;
    private final float max;
	
	public TimedDeathTrait(final float milliTillDeath) {
		super("Timed Death", "Die after a certain amount of time");
		max = milliTillDeath;
	}
	
	public TimedDeathTrait(final int secondsTillDeath) {
		this(secondsTillDeath * 1000f);
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		t = 0;
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		if ((t = delta + t) >= max) {
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
