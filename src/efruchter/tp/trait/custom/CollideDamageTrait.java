package efruchter.tp.trait.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.EntityType;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;

public class CollideDamageTrait extends Trait {
	
	private final Gene damage;
	
	public CollideDamageTrait(final float damage) {
		super("Damage Dealing", "Deals damage to another entity.");
		this.damage = new Gene("Damage", "Amount of damage to deal.", 0, damage, damage);
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {

	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		boolean hit = false;
		for (final Entity e : level.getEntities(EntityType.SHIP)) {
			if (self.isColliding(e)) {
				e.causeDamage(damage.getValue());
				hit = true;
			}
		}
		if (hit) {
		    self.health = -1;
			level.removeEntity(self);
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
