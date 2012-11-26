package efruchter.tp.traits.custom;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.entities.Ship;
import efruchter.tp.traits.Trait;

public class CollideDamageTrait extends Trait {
	
	private float damage;
	
	public CollideDamageTrait(float damage) {
		super("Damage other", "deals damage to another entity.");
		this.damage = damage;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		boolean hit = false;
		for (Entity e : level.getEntities()) {
			if (e instanceof Ship && self.isColliding(e)) {
				((Ship) e).causeDamage(damage);
				hit = true;
			}
		}
		if (hit) {
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		// TODO Auto-generated method stub
		
	}
	
}
