package efruchter.tp.entities;

import java.awt.Color;

/**
 * Entity with health.
 * 
 * @author toriscope
 * 
 */
public class Ship extends Entity {
	
	private float health;
	private long damageTimer = 0;
	
	public Ship(float health) {
		super("Ship", Color.CYAN);
		this.health = health;
	}
	
	public Ship() {
		this(10);
	}
	
	public float getHealth() {
		return health;
	}
	
	public void onUpdate(long delta, Level level) {
		super.onUpdate(delta, level);
		
		if (damageTimer > 0) {
			damageTimer--;
		}
	}
	
	public void setHealth(float newHealth) {
		health = newHealth;
	}
	
	public void causeDamage(float damage) {
		health -= damage;
		damageTimer = 16;
	}
	
	public boolean isHurtAnimFrame() {
		return damageTimer % 4 != 0;
	}
}
