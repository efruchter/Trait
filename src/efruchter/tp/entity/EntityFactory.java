package efruchter.tp.entity;

import java.awt.*;

import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.behavior.custom.KillBehavior;
import efruchter.tp.trait.custom.*;
import org.lwjgl.opengl.Display;

import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;
import efruchter.tp.util.RenderUtil;
import org.lwjgl.opengl.GL11;

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
	
	public static void buildShip(final Entity e, final float x, final float y, final float radius, final CollisionLabel collisionLabel, final Color color, final float initialHealth) {
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
        e.x = Display.getWidth() / 2 - 150;
        e.y = Display.getHeight() + 3;

        final BehaviorChain chain = new BehaviorChain();
        chain.addBehavior(CurveInterpolator.buildPath(duration, false,
                new Point.Float[]{
                        new Point.Float(e.x, e.y),
                        new Point.Float(Display.getWidth() / 2 - 150, -50)}),
                duration);
        chain.addBehavior(new KillBehavior(), 0);

        e.addTrait(chain);

        e.setRenderBehavior(new Behavior() {
            public void onStart(Entity self, Level level) {}
            public void onUpdate(Entity self, Level level, long delta) {
                GL11.glPushMatrix();
                {
                    RenderUtil.setColor(Color.GREEN);
                    GL11.glTranslatef(self.x, self.y, 0);
                    RenderUtil.drawString("NEW WAVE ", 5);
                }
                GL11.glPopMatrix();
            }
            public void onDeath(Entity self, Level level) {}
        });

        e.health = 0;
        
        return e;
    }
}
