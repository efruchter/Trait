package efruchter.tp.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.traits.Trait;
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
	public int collisionLabel;
	public float health;
	public EntityType entityType;
	
	private Behavior renderBehavior;
	private List<Trait> traits;
	private long damageTimer = 0;
	
	public Entity() {
		this.name = "NO_NAME";
		this.baseColor = Color.BLACK;
		x = y = radius = 0;
		traits = new ArrayList<Trait>();
		collisionLabel = CollisionLabels.DEFAULT;
		entityType = EntityType.NO_TYPE;
		setRenderBehavior(Behavior.EMPTY);
	}
	
	public void onStart(Level level) {
		for (Behavior b : traits) {
			b.onStart(this, level);
		}
	}
	
	public void onDeath(Level level) {
		for (Behavior b : traits) {
			b.onDeath(this, level);
		}
	}
	
	public void addTrait(Trait trait) {
		traits.add(trait);
	}
	
	public List<Trait> getTraits() {
		return traits;
	}
	
	public Behavior getRenderBehavior() {
		return renderBehavior;
	}
	
	public void setRenderBehavior(Behavior renderBehavior) {
		this.renderBehavior = renderBehavior;
	}
	
	public boolean isColliding(Entity other) {
		return collisionLabel != other.collisionLabel && ScriptUtil.isColliding(this, other);
	}
	
	public float getHealth() {
		return health;
	}
	
	public void onUpdate(long delta, Level level) {
		for (Behavior b : traits) {
			b.onUpdate(this, level, delta);
		}
		
		if (damageTimer > 0) {
			damageTimer--;
		}
	}
	
	public void setHealth(float newHealth) {
		health = newHealth;
	}
	
	public void causeDamage(float damage) {
		health -= damage;
		damageTimer = 16 - damageTimer % 4;
	}
	
	public boolean isHurtAnimFrame() {
		return damageTimer % 4 != 0;
	}
}
