package efruchter.tp.trait.custom;


import efruchter.tp.TraitProjectClient;
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
		if (self.x > TraitProjectClient.SIZE.width) {
			self.x = self.x - TraitProjectClient.SIZE.width;
		} else if (self.x < 0) {
			self.x = TraitProjectClient.SIZE.width + self.x;
		}
		
		if (self.y > TraitProjectClient.SIZE.height) {
			self.y = self.y - TraitProjectClient.SIZE.height;
		} else if (self.y < 0) {
			self.y = TraitProjectClient.SIZE.height + self.y;
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
