package efruchter.tp.entities;

import java.awt.Color;

import efruchter.tp.util.RenderUtil;

/**
 * Standard projective. Not unlike an entity. More differences to come.
 * 
 * @author toriscope
 * 
 */
public class Projectile extends Entity {
	
	public Projectile(float x, float y, float r) {
		super("Projectile");
		this.x = x;
		this.y = y;
		this.radius = r;
		setRenderBehavior(RenderUtil.getShipRenderer(Color.GREEN));
	}
	
}
