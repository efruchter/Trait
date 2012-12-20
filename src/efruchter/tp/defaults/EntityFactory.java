package efruchter.tp.defaults;

import java.awt.Color;

import efruchter.tp.trait.custom.*;
import org.lwjgl.opengl.Display;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
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
	public static void buildProjectile(final Entity e, final float x, final float y, final float radius, final CollisionLabel collisionLabel, final Color color, final float damage) {
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.PROJECTILE;
		e.setRenderBehavior(RenderUtil.PROJECTILE_RENDER);
		e.addTrait(new CollideDamageTrait(damage));
		e.addTrait(new DramaticDeathTrait(5, 200));
	}
	
	public static void buildShip(final Entity e, final float x, final float y, final float radius, final CollisionLabel collisionLabel, final Color color, final int initialHealth) {
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.SHIP;
		e.setRenderBehavior(RenderUtil.SHIP_RENDER);
		e.health = initialHealth;
		e.addTrait(new NoHealthDeathTrait());
		e.addTrait(new DramaticDeathTrait(10, 1000));
	}
	
	/**
	 * Generate a background star.
	 * 
	 * @return
	 */
	public static void buildBackgroundStar(final Entity e) {
		e.baseColor = Math.random() < .99 ? new Color(82, 82, 82) : Color.WHITE;
		e.collisionLabel = CollisionLabel.NO_COLLISION;
		e.entityType = EntityType.BG;
		e.setRenderBehavior(RenderUtil.STAR_RENDER);
		e.health = 0;
		e.addTrait(starMove);
	}
	
	/**
	 * Move pattern for stars.
	 */
	private final static Trait starMove = new Trait("Star Move", "Loop and scale.") {
		float dx = 0;

		@Override
		public void onStart(final Entity self, final Level level) {
			self.radius = .5f + (float) Math.random() * 3;
			self.x = ((float) Math.random()) * Display.getWidth();
			self.y = ((float) Math.random()) * Display.getHeight();
		}
		
		@Override
		public void onUpdate(final Entity self, final Level level, final long delta) {
			self.y -= self.radius;
            self.x += dx;

            boolean shifted = false;
            if (self.x > Display.getWidth()) {
                self.x = self.x - Display.getWidth();
                shifted = true;
            } else if (self.x < 0) {
                self.x = Display.getWidth() + self.x;
                shifted = true;
            }

            if(shifted) {
                self.y = ((float) Math.random()) * Display.getHeight();
                self.radius = .5f + (float) Math.random() * 3;
            }

            shifted = false;
            if (self.y > Display.getHeight()) {
                self.y = self.y - Display.getHeight();
                shifted = true;
            } else if (self.y < 0) {
                self.y = Display.getHeight() + self.y;
                shifted = true;
            }

            if(shifted) {
                self.x = ((float) Math.random()) * Display.getWidth();
                self.radius = .5f + (float) Math.random() * 3;
            }
		}
		
		@Override
		public void onDeath(final Entity self, final Level level) {

		}
	};
	
	public static void buildExplosion(final Entity e, final float x, final float y, final float radius, final Color color, final long delay) {
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.entityType = EntityType.BG;
		e.setRenderBehavior(RenderUtil.PROJECTILE_RENDER);
		e.addTrait(new DieOffScreenTrait());
		e.addTrait(new TimedDeathTrait(delay));
		
		final WiggleTrait w = new WiggleTrait(20);
		w.wiggleChance.setExpression(1);
		w.wiggleIntensity.setExpression(1);
		e.addTrait(w);
		
		final RadiusEditTrait rad = new RadiusEditTrait(0, radius, radius);
		e.addTrait(rad);
		e.addTrait(new GeneExpressionInterpolator(rad.radius, 1, 0, (long) (Math.random() * delay)));
	}
}
