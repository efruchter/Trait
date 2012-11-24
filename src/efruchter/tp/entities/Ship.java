package efruchter.tp.entities;

/**
 * Entity with health.
 * 
 * @author toriscope
 * 
 */
public class Ship extends Entity {
	
	protected float health;
	
	public Ship(float health) {
		super("Ship");
		this.health = health;
	}
	
	public Ship() {
		this(10);
	}
}
