package efruchter.tp.trait.behavior;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;

/**
 * A behavior an entity should perform. Three timing methods are called given
 * certain game events.
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
	public void onStart(Entity self, Level level);
	
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
	public void onUpdate(Entity self, Level level, long delta);
	
	/**
	 * Called on death of the entity.
	 * 
	 * @param self
	 *            the entity to affect.
	 * @param level
	 *            the level the entity inhabits.
	 */
	public void onDeath(Entity self, Level level);
	
	/**
	 * Default behavior. Does nothing.
	 */
	public static Behavior EMPTY = new Behavior() {
		
		@Override
		public void onStart(Entity self, Level level) {
			
		}
		
		@Override
		public void onUpdate(Entity self, Level level, long delta) {
			
		}
		
		@Override
		public void onDeath(Entity self, Level level) {
			
		}
		
		public String toString() {
			return "(B) " + "EMPTY";
		}
	};
	
	/**
	 * Wait behavior. Does nothing.
	 */
	public static Behavior WAIT = new Behavior() {
		
		@Override
		public void onStart(Entity self, Level level) {
			
		}
		
		@Override
		public void onUpdate(Entity self, Level level, long delta) {
			
		}
		
		@Override
		public void onDeath(Entity self, Level level) {
			
		}
		
		public String toString() {
			return "(B) " + WAIT;
		}
	};
}
