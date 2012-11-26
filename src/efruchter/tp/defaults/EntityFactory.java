package efruchter.tp.defaults;

import java.awt.Color;

import efruchter.tp.entities.Entity;
import efruchter.tp.traits.custom.CollideDamageTrait;
import efruchter.tp.traits.custom.NoHealthDeathTrait;
import efruchter.tp.util.RenderUtil;

/**
 * Methods for creating canned entities that conform to a certain type
 * description.
 * 
 * @author toriscope
 * 
 */
public class EntityFactory {
	public static Entity buildProjectile(float x, float y, int radius, int collisionLabel, Color color, float damage) {
		Entity e = new Entity();
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.PROJECTILE;
		e.setRenderBehavior(RenderUtil.GENERIC_RENDER);
		e.addTrait(new CollideDamageTrait(damage));
		return e;
	}
	
	public static Entity buildShip(float x, float y, int radius, int collisionLabel, Color color, int initialHealth) {
		Entity e = new Entity();
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.SHIP;
		e.setRenderBehavior(RenderUtil.GENERIC_RENDER);
		e.health = initialHealth;
		e.addTrait(new NoHealthDeathTrait());
		return e;
	}
}
