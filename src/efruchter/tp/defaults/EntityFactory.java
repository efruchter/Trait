package efruchter.tp.defaults;

import java.awt.Color;

import efruchter.tp.entity.Entity;
import efruchter.tp.trait.custom.CollideDamageTrait;
import efruchter.tp.trait.custom.DieOffScreenTrait;
import efruchter.tp.trait.custom.DramaticDeathTrait;
import efruchter.tp.trait.custom.NoHealthDeathTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TimedDeathTrait;
import efruchter.tp.trait.custom.WiggleTrait;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;
import efruchter.tp.util.RenderUtil;

/**
 * Methods for creating canned entities that conform to a certain type
 * description.
 * 
 * @author toriscope
 * 
 */
public class EntityFactory {
	public static Entity buildProjectile(float x, float y, float radius, int collisionLabel, Color color, float damage) {
		Entity e = new Entity();
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.PROJECTILE;
		e.setRenderBehavior(RenderUtil.GENERIC_RENDER);
		e.addTrait(new CollideDamageTrait(damage));
		
		e.addTrait(new DramaticDeathTrait(10, 200));
		return e;
	}
	
	public static Entity buildShip(float x, float y, float radius, int collisionLabel, Color color, int initialHealth) {
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
		e.addTrait(new DramaticDeathTrait(10, 1000));
		return e;
	}
	
	public static Entity buildExplosion(float x, float y, float radius, Color color, long delay) {
		Entity e = new Entity();
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.entityType = EntityType.PROJECTILE;
		e.setRenderBehavior(RenderUtil.GENERIC_RENDER);
		e.addTrait(new DieOffScreenTrait());
		e.addTrait(new TimedDeathTrait(delay));
		
		WiggleTrait w = new WiggleTrait(20);
		w.wiggleChance.setExpression(1);
		w.wiggleIntensity.setExpression(1);
		e.addTrait(w);
		
		RadiusEditTrait rad = new RadiusEditTrait(0, radius, radius);
		e.addTrait(rad);
		e.addTrait(new GeneExpressionInterpolator(rad.radius, 1, 0, (long) (Math.random() * delay)));
		
		return e;
	}
}
