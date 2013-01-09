package efruchter.tp.trait.custom;

import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * Blow up in a fiery death upon death.
 * 
 * @author toriscope
 * 
 */
public class DramaticDeathTrait extends Trait {
	
	private final int drama;
	private final long delay;
	
	/**
	 * Standard 'splosion.
	 * 
	 * @param dramaLevel
	 *            how many booms.
	 * @param delay
	 *            how long the splosions last in milli.
	 */
	public DramaticDeathTrait(final int dramaLevel, final long delay) {
		super("Dramatic Death", "Spawn confetti upon unit death.");
		this.drama = dramaLevel;
		this.delay = delay;
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
	    if (self.health < 0) {
            for (int i = 0; i < drama; i++) {
                final Entity e = level.getBlankEntity(EntityType.BG);
                EntityFactory.buildExplosion(e, self.x, self.y, self.radius, self.baseColor, delay);
            }
	    }
	}
	
}
