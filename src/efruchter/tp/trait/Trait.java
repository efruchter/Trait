package efruchter.tp.trait;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.behavior.Behavior;

/**
 * Trait, composed of genes. This should convert its genes into some sort of
 * behavior. Genes should be exposed for editing. A Heavy-duty behavior meant to
 * 
 * @author toriscope
 * 
 */
public abstract class Trait implements Behavior {
	
	private final String name, info;
	private boolean active;
	
	public Trait(final String name, final String info) {
		this.name = name;
		this.info = info;
		active = true;
	}
	
	public String getName() {
		return name;
	}

	public String getInfo() {
		return info;
	}
	
	@Override
	public String toString() {
		return "(T) " + name + " (" + (isActive() ? "ON" : "OFF") + ")";
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(final boolean active) {
		this.active = active;
	}
	
	public class TraitAdapter extends Trait {
		
		public TraitAdapter(String name, String info) {
			super(name, info);
		}
		
		@Override
		public void onStart(final Entity self, final Level level) {
			
		}
		
		@Override
		public void onUpdate(final Entity self, final Level level, final long delta) {
			
		}
		
		@Override
		public void onDeath(final Entity self, final Level level) {
			
		}
	}
}
