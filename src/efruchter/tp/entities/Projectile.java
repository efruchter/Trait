package efruchter.tp.entities;

import java.awt.Color;

/**
 * Standard projective. Not unlike an entity. More differences to come.
 * 
 * @author toriscope
 * 
 */
public class Projectile extends Entity {
	
	public Projectile(float x, float y, float r) {
		super("Projectile", Color.GREEN);
		this.x = x;
		this.y = y;
		this.radius = r;
	}
	
}
