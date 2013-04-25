package efruchter.tp.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;


import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.util.ScriptUtil;

/**
 * Entity in the game world, with behaviors, a position and a radius.
 * 
 * @author toriscope
 * 
 */
public class Entity {
	
	public float x, y, radius;
	public String name;
	public Color baseColor;
	public CollisionLabel collisionLabel;
	public int polarity = -1;
	public float health;
	public EntityType entityType;
	private boolean active, hasStarted;
	
	private Behavior renderBehavior;
	private final List<Trait> traits;
	private long damageTimer = 0;
	
	private static long activeEntities = 0;
	
	public Entity() {
		traits = new ArrayList<Trait>();
		reset();
	}
	
	private void onStart(final Level level) {
		if (active) {
			for (Trait b : traits) {
				if (b.isActive())
					b.onStart(this, level);
			}
		}
	}
	
	public void onUpdate(final long delta, final Level level) {
		if (active) {
			if (hasStarted) {
				for (Trait b : traits) {
					if (b.isActive()) {
						b.onUpdate(this, level, delta);
					}
				}
				if (damageTimer > 0) {
					damageTimer--;
				}
			} else {
				onStart(level);
				hasStarted = true;
			}
		}
	}
	
	public void onDeath(final Level level) {
		if (active) {
			for (Trait b : traits) {
				if (b.isActive())
					b.onDeath(this, level);
			}
		}
	}
	
	public void addTrait(final Trait trait) {
		traits.add(trait);
	}
	
	public boolean removeTrait(final Trait trait) {
		return traits.remove(trait);
	}
	
	public List<Trait> getTraits() {
		return traits;
	}
	
	public Behavior getRenderBehavior() {
		return renderBehavior;
	}
	
	public void setRenderBehavior(final Behavior renderBehavior) {
		this.renderBehavior = renderBehavior;
	}
	
	public boolean isColliding(final Entity other) {
	    boolean samePolarity = polarity == -1 ? false : (polarity == other.polarity);
		return !samePolarity && other.collisionLabel != CollisionLabel.NO_COLLISION && collisionLabel != other.collisionLabel
				&& ScriptUtil.isColliding(this, other);
	}
	
	public float getHealth() {
		return health;
	}
	
	public void setHealth(final float newHealth) {
		health = newHealth;
	}
	
	public void causeDamage(final float damage) {
		health -= damage;
		damageTimer = 8 - damageTimer % 4;
	}
	
	public boolean isHurtAnimFrame() {
		return damageTimer % 4 != 0;
	}
	
	@Override
	public String toString() {
		return "(E) " + name;
	}
	
	public void setActive(final boolean active) {
		if (this.active != active)
			activeEntities += (active) ? 1 : -1;
		this.active = active;
		
	}
	
	public boolean isActive() {
		return active;
	}
	
	public static long getActiveEntityCount() {
		return activeEntities;
	}
	
	public void reset() {
		this.name = "";
		this.baseColor = Color.BLACK;
		x = y = radius = 0;
		health = 10;
		traits.clear();
		collisionLabel = CollisionLabel.NO_COLLISION;
		entityType = EntityType.NONE;
		setRenderBehavior(Behavior.EMPTY);
		setActive(hasStarted = false);
	}
	
	public void draw(final Graphics g) {
	    g.setColor(baseColor);
	    g.fillOval((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
	}
}
