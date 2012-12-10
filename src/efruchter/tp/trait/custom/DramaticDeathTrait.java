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
	
	private int drama;
	private long delay;
	
	/**
	 * Standard 'splosion.
	 * 
	 * @param dramaLevel
	 *            how many booms.
	 * @param delay
	 *            how long the splosions last in milli.
	 */
	public DramaticDeathTrait(int dramaLevel, long delay) {
		super("Dramatic Death", "Spawn confetti upon unit death.");
		this.drama = dramaLevel;
		this.delay = delay;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		for (int i = 0; i < drama; i++) {
			Entity e = level.getBlankEntity(EntityType.BG);
			EntityFactory.buildExplosion(e, self.x, self.y, self.radius, self.baseColor, delay);
		}
	}
	
}
