package efruchter.tp.trait.behavior;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;

/**
 * A behavior an entity should perform. Three timing methods are called given
 * certain game events. The mose basic form of functional unit.
 * 
 * @author toriscope
 * 
 */
public interface Behavior {
	
	/**
	 * Called on initialization of the entity.
	 * 
	 * @param self
	 *            the entity to affect.
	 * @param level
	 *            the level the entity inhabits.
	 */
	public void onStart(final Entity self, final Level level);
	
	/**
	 * Called on update of the entity.
	 * 
	 * @param self
	 *            the entity to affect.
	 * @param level
	 *            the level the entity inhabits.
	 * @param delta
	 *            frame time in milliseconds
	 */
	public void onUpdate(final Entity self, final Level level, final long delta);
	
	/**
	 * Called on death of the entity.
	 * 
	 * @param self
	 *            the entity to affect.
	 * @param level
	 *            the level the entity inhabits.
	 */
	public void onDeath(final Entity self, final Level level);
	
	/**
	 * Default behavior. Does nothing.
	 */
	public final static Behavior EMPTY = new Behavior() {
		
		@Override
		public void onStart(final Entity self, final Level level) {
			
		}
		
		@Override
		public void onUpdate(final Entity self, final Level level, final long delta) {
			
		}
		
		@Override
		public void onDeath(final Entity self, final Level level) {
			
		}
		
		public String toString() {
			return "(B) " + "EMPTY";
		}
	};
}
