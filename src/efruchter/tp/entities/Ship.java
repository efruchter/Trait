package efruchter.tp.entities;

import java.awt.Color;

/**
 * Entity with health.
 * 
 * @author toriscope
 * 
 */
public class Ship extends Entity {
	
	protected float health;
	
	public Ship(float health) {
		super("Ship", Color.CYAN);
		this.health = health;
	}
	
	public Ship() {
		this(10);
	}
}
