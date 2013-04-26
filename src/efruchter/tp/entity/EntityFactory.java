package efruchter.tp.entity;

import java.awt.*;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.behavior.custom.KillBehavior;
import efruchter.tp.trait.custom.*;


import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;


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
		e.addTrait(new CollideDamageTrait(damage));
		e.addTrait(new DramaticDeathTrait(5, 200));
	}
	
	public static void buildShip(final Entity e, final float x, final float y, final float radius, final CollisionLabel collisionLabel, final Color color, final float initialHealth) {
		e.x = x;
		e.y = y;
		e.radius = radius;
		e.baseColor = color;
		e.collisionLabel = collisionLabel;
		e.entityType = EntityType.SHIP;
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
			self.x = ((float) Math.random()) * TraitProjectClient.SIZE.width;
			self.y = ((float) Math.random()) * TraitProjectClient.SIZE.height;
		}
		
		@Override
		public void onUpdate(final Entity self, final Level level, final long delta) {
			self.y -= self.radius;
            self.x += dx;

            boolean shifted = false;
            if (self.x > TraitProjectClient.SIZE.width) {
                self.x = self.x - TraitProjectClient.SIZE.width;
                shifted = true;
            } else if (self.x < 0) {
                self.x = TraitProjectClient.SIZE.width + self.x;
                shifted = true;
            }

            if(shifted) {
                self.y = ((float) Math.random()) * TraitProjectClient.SIZE.height;
                self.radius = .5f + (float) Math.random() * 3;
            }

            shifted = false;
            if (self.y > TraitProjectClient.SIZE.height) {
                self.y = self.y - TraitProjectClient.SIZE.height;
                shifted = true;
            } else if (self.y < 0) {
                self.y = TraitProjectClient.SIZE.height + self.y;
                shifted = true;
            }

            if(shifted) {
                self.x = ((float) Math.random()) * TraitProjectClient.SIZE.width;
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
		e.addTrait(new TimedDeathTrait(delay));
		
		final WiggleTrait w = new WiggleTrait(20);
		w.wiggleChance.setExpression(1);
		w.wiggleIntensity.setExpression(1);
		e.addTrait(w);
		
		final RadiusEditTrait rad = new RadiusEditTrait(0, radius, radius);
		e.addTrait(rad);
		e.addTrait(new GeneExpressionInterpolator(rad.radius, 1, 0, (long) (Math.random() * delay)));
	}

    public static Entity buildNewWaveAnim(final Entity e) {

        final long duration = 7000;

        e.baseColor = Color.CYAN;
        e.collisionLabel = CollisionLabel.NO_COLLISION;
        e.entityType = EntityType.BG;
        e.x = TraitProjectClient.SIZE.width / 2 - 150;
        e.y = TraitProjectClient.SIZE.height + 3;

        final BehaviorChain chain = new BehaviorChain();
        chain.addBehavior(CurveInterpolator.buildPath(duration, false,
                new Point.Float[]{
                        new Point.Float(e.x, e.y),
                        new Point.Float(TraitProjectClient.SIZE.width / 2 - 150, -50)}),
                duration);
        chain.addBehavior(new KillBehavior(), 0);

        e.addTrait(chain);

        e.setRenderBehavior(new Behavior() {
            public void onStart(Entity self, Level level) {}
            public void onUpdate(Entity self, Level level, long delta) {
                /*GL11.glPushMatrix();
                {
                    RenderUtil.setColor(Color.GREEN);
                    GL11.glTranslatef(self.x, self.y, 0);
                    RenderUtil.drawString("NEW WAVE ", 5);
                }
                GL11.glPopMatrix();*/
            }
            public void onDeath(Entity self, Level level) {}
        });

        e.health = 0;
        
        return e;
    }
}
