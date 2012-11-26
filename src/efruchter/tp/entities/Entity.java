package efruchter.tp.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import efruchter.tp.traits.Trait;
import efruchter.tp.util.RenderUtil;
import efruchter.tp.util.ScriptUtil;

/**
 * Entity in the game world, with behaviors, a position and a radius.
 * 
 * @author toriscope
 * 
 */
public abstract class Entity {
	
	public float x, y, radius;
	public String name;
	public Color baseColor;
	public int collisionLabel = 0;
	public int typeLabel = 0;
	
	private Behavior renderBehavior;
	private List<Trait> traits;
	
	public Entity(String name, Color baseColor) {
		x = y = radius = 0;
		setRenderBehavior(Behavior.EMPTY);
		traits = new ArrayList<Trait>();
		this.baseColor = baseColor;
		this.setRenderBehavior(RenderUtil.getGenericRenderBehavior());
	}
	
	public void onStart(Level level) {
		for (Behavior b : traits) {
			b.onStart(this, level);
		}
	}
	
	public void onUpdate(long delta, Level level) {
		for (Behavior b : traits) {
			b.onUpdate(this, level, delta);
		}
	}
	
	public void onDeath(Level level) {
		for (Behavior b : traits) {
			b.onDeath(this, level);
		}
	}
	
	public void addTrait(Trait trait, Level level) {
		traits.add(trait);
		trait.onStart(this, level);
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
	
	public final static Entity BLANK = new Entity("", Color.BLACK) {
		//Blank entity
	};
	
}
