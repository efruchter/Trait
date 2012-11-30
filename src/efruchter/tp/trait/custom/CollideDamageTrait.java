package efruchter.tp.trait.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;

public class CollideDamageTrait extends Trait {
	
	private Gene damage;
	
	public CollideDamageTrait(float damage) {
		super("Damage Dealing", "Deals damage to another entity.");
		registerGene(this.damage = new Gene("Damage", "Amount of damage to deal.", 0, damage, damage));
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		boolean hit = false;
		for (Entity e : level.getShips()) {
			if (self.isColliding(e)) {
				e.causeDamage(damage.getValue());
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
