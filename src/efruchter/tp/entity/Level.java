package efruchter.tp.entity;

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
	private List<LevelListener> listeners;
	
	public Level() {
		ships = new LinkedList<Entity>();
		bullets = new LinkedList<Entity>();
		listeners = new LinkedList<LevelListener>();
	}
	
	public void onStart() {
		for (Entity b : new LinkedList<Entity>(ships)) {
			b.onStart(this);
		}
		
		for (Entity b : new LinkedList<Entity>(bullets)) {
			b.onStart(this);
		}
	}
	
	public void addLevelListener(LevelListener b) {
		listeners.add(b);
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
		for (Entity b : new LinkedList<Entity>(ships)) {
			b.onDeath(this);
		}
		
		for (Entity b : new LinkedList<Entity>(bullets)) {
			b.onDeath(this);
		}
		
	}
	
	public void renderGL(long delta) {
		for (Entity b : ships) {
			b.getRenderBehavior().onUpdate(b, this, delta);
		}
		
		for (Entity b : bullets) {
			b.getRenderBehavior().onUpdate(b, this, delta);
		}
	}
	
	public void removeEntity(Entity p) {
		switch (p.entityType) {
			case SHIP:
				ships.remove(p);
				for (LevelListener listener : listeners)
					listener.shipRemoved(p);
				break;
			case PROJECTILE:
				bullets.remove(p);
				for (LevelListener listener : listeners)
					listener.bulletRemoved(p);
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
				for (LevelListener listener : listeners)
					listener.shipAdded(p);
				break;
			case PROJECTILE:
				bullets.add(p);
				for (LevelListener listener : listeners)
					listener.bulletAdded(p);
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
	
	@Override
	public String toString() {
		return "Level";
	}
	
	public interface LevelListener {
		void shipRemoved(Entity ship);
		
		void shipAdded(Entity ship);
		
		void bulletAdded(Entity bullet);
		
		void bulletRemoved(Entity bullet);
	}
}
