package efruchter.tp.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * Stores and coordinates entities.
 * 
 * @author toriscope
 * 
 */
public class Level {
	
	private List<Entity> ships;
	private List<Entity> bullets;
	
	public Level() {
		ships = new LinkedList<Entity>();
		bullets = new LinkedList<Entity>();
	}
	
	public void onStart() {
		for (Entity b : ships) {
			b.onStart(this);
		}
		
		for (Entity b : bullets) {
			b.onStart(this);
		}
	}
	
	public void onUpdate(long delta) {
		for (Entity b : new LinkedList<Entity>(bullets)) {
			b.onUpdate(delta, this);
		}
		
		for (Entity b : new LinkedList<Entity>(ships)) {
			b.onUpdate(delta, this);
		}
	}
	
	public void onDeath() {
		for (Entity b : ships) {
			b.onDeath(this);
		}
		
		for (Entity b : bullets) {
			b.onDeath(this);
		}
		
	}
	
	public void renderGL() {
		for (Entity b : ships) {
			b.getRenderBehavior().onUpdate(b, this, 0);
		}
		
		for (Entity b : bullets) {
			b.getRenderBehavior().onUpdate(b, this, 0);
		}
	}
	
	public void removeEntity(Entity p) {
		switch (p.entityType) {
			case SHIP:
				ships.remove(p);
				break;
			case PROJECTILE:
				bullets.remove(p);
				break;
			default:
				throw new RuntimeException("Entity with NO_TYPE encountered.");
		}
		
		p.onDeath(this);
	}
	
	public void addEntity(Entity p) {
		switch (p.entityType) {
			case SHIP:
				ships.add(p);
				break;
			case PROJECTILE:
				bullets.add(p);
				break;
			default:
				throw new RuntimeException("Entity with NO_TYPE encountered.");
		}
		
		p.onStart(this);
	}
	
	public List<Entity> getShips() {
		return ships;
	}
	
	public List<Entity> getBullets() {
		return bullets;
	}
	
	public int getEntityCount() {
		return ships.size() + bullets.size();
	}
}
