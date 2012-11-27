package efruchter.tp.trait.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class CollideDamageTrait extends Trait {
	
	private float damage;
	
	public CollideDamageTrait(float damage) {
		super("Damage Dealing", "Deals damage to another entity.");
		this.damage = damage;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		boolean hit = false;
		for (Entity e : level.getShips()) {
			if (self.isColliding(e)) {
				e.causeDamage(damage);
				hit = true;
			}
		}
		if (hit) {
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
