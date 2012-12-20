package efruchter.tp.trait.custom;

import org.lwjgl.opengl.Display;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class LoopScreenTrait extends Trait {
	
	public LoopScreenTrait() {
		super("Loop Screen Around Edges", "Cause entity to loop when offscreen.");
		
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		// TODO: Cheap fix, make better.
		if (self.x > Display.getWidth()) {
			self.x = self.x - Display.getWidth();
		} else if (self.x < 0) {
			self.x = Display.getWidth() + self.x;
		}
		
		if (self.y > Display.getHeight()) {
			self.y = self.y - Display.getHeight();
		} else if (self.y < 0) {
			self.y = Display.getHeight() + self.y;
		}
		
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
