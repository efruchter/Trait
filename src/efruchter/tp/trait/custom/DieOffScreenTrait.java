package efruchter.tp.trait.custom;

import org.lwjgl.opengl.Display;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class DieOffScreenTrait extends Trait {
	
	public DieOffScreenTrait() {
		super("Die Offscreen", "Cause entity to die when offscreen.");
		
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		
		if (self.x < 0 || self.x > Display.getWidth() || self.y > Display.getHeight() || self.y < 0) {
			level.removeEntity(self);
		}
		
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
