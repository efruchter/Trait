package efruchter.tp.entity;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import efruchter.tp.trait.behavior.Behavior;

/**
 * Stores and coordinates entities.
 * 
 * @author toriscope
 * 
 */
public class Level {
	
	private final List<Entity> ships;
	private final List<Entity> bullets;
	private final List<Entity> notypes;
	private final List<LevelListener> listeners;
	
	private final List<Behavior> renderBehaviors;
	
	public Level() {
		ships = new LinkedList<Entity>();
		bullets = new LinkedList<Entity>();
		notypes = new LinkedList<Entity>();
		listeners = new LinkedList<LevelListener>();
		renderBehaviors = new LinkedList<Behavior>();
	}
	
	public void addRenderBehavior(Behavior beh) {
		renderBehaviors.add(beh);
	}
	
	public void onStart() {
		for (Entity b : new LinkedList<Entity>(ships)) {
			b.onStart(this);
		}
		
		for (Entity b : new LinkedList<Entity>(bullets)) {
			b.onStart(this);
		}
		
		for (Entity b : new LinkedList<Entity>(notypes)) {
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
		
		for (Entity b : new LinkedList<Entity>(notypes)) {
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
		
		for (Entity b : new LinkedList<Entity>(notypes)) {
			b.onDeath(this);
		}
		
	}
	
	public void addLevelListener(LevelListener b) {
		listeners.add(b);
	}
	
	public void renderGL(long delta) {
		
		for (Behavior b : renderBehaviors) {
			b.onUpdate(null, this, delta);
		}
		
		try {
			
			for (Entity b : notypes) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}
			
			for (Entity b : ships) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}
			
			for (Entity b : bullets) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}
			
		} catch (ConcurrentModificationException e) {
			
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
			case NO_TYPE:
				notypes.remove(p);
				break;
			default:
				throw new RuntimeException("Entity with strange type encountered.");
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
			case NO_TYPE:
				notypes.add(p);
				break;
			default:
				throw new RuntimeException("Entity with strange type encountered.");
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
	
	public static abstract class LevelListener {
		public void shipRemoved(Entity ship) {
		}
		
		public void shipAdded(Entity ship) {
		}
		
		public void bulletAdded(Entity bullet) {
		}
		
		public void bulletRemoved(Entity bullet) {
		}
	}
}
