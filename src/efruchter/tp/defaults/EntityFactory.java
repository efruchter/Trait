package efruchter.tp.defaults;

import java.awt.Color;

import org.lwjgl.opengl.Display;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
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
	
	/**
	 * Generate a background star.
	 * 
	 * @return
	 */
	public static Entity buildBackgroundStar() {
		Entity e = new Entity();
		e.baseColor = Math.random() < .99 ? new Color(82, 82, 82) : Color.WHITE;
		e.collisionLabel = CollisionLabels.NO_COLLISION;
		e.entityType = EntityType.NO_TYPE;
		e.setRenderBehavior(RenderUtil.GENERIC_RENDER);
		e.health = 0;
		
		e.addTrait(starMove);
		
		return e;
	}
	
	/**
	 * Move pattern for stars.
	 */
	private static Trait starMove = new Trait("Move star", " star move") {
		
		@Override
		public void onStart(Entity self, Level level) {
			self.radius = .5f + (float) Math.random() * 3;
			self.x = ((float) Math.random()) * Display.getWidth();
			self.y = ((float) Math.random()) * Display.getHeight();
		}
		
		@Override
		public void onUpdate(Entity self, Level level, long delta) {
			if (self.y - self.radius < 0) {
				self.radius = .5f + (float) Math.random() * 3;
				self.x = ((float) Math.random()) * Display.getWidth();
				self.y = Display.getHeight() + self.radius;
			}
			
			self.y -= self.radius;
		}
		
		@Override
		public void onDeath(Entity self, Level level) {
			
		}
		
	};
	
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
