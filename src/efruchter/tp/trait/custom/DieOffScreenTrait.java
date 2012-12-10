package efruchter.tp.trait.custom;

import org.lwjgl.opengl.Display;

import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class DieOffScreenTrait extends Trait {
	
	public DieOffScreenTrait() {
		super("Die Offscreen", "Cause entity to die when offscreen.");
		
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		
		if (self.x < 0 || self.x > Display.getWidth() || self.y > Display.getHeight() || self.y < 0) {
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
