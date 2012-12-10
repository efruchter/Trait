package efruchter.tp.entity;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import efruchter.tp.defaults.EntityType;
import efruchter.tp.learning.GeneVector;
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
	private final List<Entity> bgs;
	private final List<Entity> add;
	private final List<Entity> remove;
	private final List<Entity> shipRecycle, bulletRecycle, bgRecycle;
	private final List<LevelListener> listeners;
	private final GeneVector explorationVector;
	private final List<Behavior> renderBehaviors;
	
	public Level() {
		ships = new LinkedList<Entity>();
		bullets = new LinkedList<Entity>();
		bgs = new LinkedList<Entity>();
		add = new LinkedList<Entity>();
		remove = new LinkedList<Entity>();
		shipRecycle = new LinkedList<Entity>();
		bulletRecycle = new LinkedList<Entity>();
		bgRecycle = new LinkedList<Entity>();
		listeners = new LinkedList<LevelListener>();
		renderBehaviors = new LinkedList<Behavior>();
		explorationVector = GeneVector.getExplorationVector();
	}
	
	public void addRenderBehavior(Behavior beh) {
		renderBehaviors.add(beh);
	}
	
	public void onUpdate(long delta) {
		for (Entity b : bullets) {
			b.onUpdate(delta, this);
		}
		
		for (Entity b : ships) {
			b.onUpdate(delta, this);
		}
		
		for (Entity b : bgs) {
			b.onUpdate(delta, this);
		}
		
		maint();
	}
	
	public void onDeath() {
		for (Entity b : ships) {
			b.onDeath(this);
		}
		
		for (Entity b : bullets) {
			b.onDeath(this);
		}
		
		for (Entity b : bgs) {
			b.onDeath(this);
		}
	}
	
	private void maint() {
		if (!add.isEmpty()) {
			for (Entity a : add) {
				addEntityUnsafe(a);
			}
			add.clear();
		}
		if (!remove.isEmpty()) {
			for (Entity a : remove) {
				removeEntityUnsafe(a);
			}
			remove.clear();
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
			
			for (Entity b : bgs) {
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
		remove.add(p);
	}
	
	private void removeEntityUnsafe(Entity p) {
		switch (p.entityType) {
			case SHIP: {
				shipRecycle.add(p);
				for (LevelListener listener : listeners)
					listener.shipRemoved(p);
				break;
			}
			case PROJECTILE: {
				bulletRecycle.add(p);
				for (LevelListener listener : listeners)
					listener.bulletRemoved(p);
				break;
			}
			case BG: {
				bgRecycle.add(p);
				break;
			}
			default:
				throw new RuntimeException("Entity with strange type encountered." + p.entityType);
		}
		p.onDeath(this);
		p.reset();
	}
	
	private void addEntityUnsafe(Entity p) {
		switch (p.entityType) {
			case SHIP: {
				ships.add(p);
				for (LevelListener listener : listeners)
					listener.shipAdded(p);
				break;
			}
			case PROJECTILE: {
				bullets.add(p);
				for (LevelListener listener : listeners)
					listener.bulletAdded(p);
				break;
			}
			case BG: {
				bgs.add(p);
				break;
			}
			default:
				throw new RuntimeException("Entity with strange type encountered.");
		}
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
	
	public GeneVector getExplorationVector() {
		return explorationVector;
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
	
	/**
	 * Get a blank entity, automatically added to the level.
	 * 
	 * @return
	 */
	public Entity getBlankEntity(final EntityType type) {
		List<Entity> l;
		switch (type) {
			case SHIP: {
				l = shipRecycle;
				break;
			}
			case PROJECTILE: {
				l = bulletRecycle;
				break;
			}
			case BG: {
				l = bgRecycle;
				break;
			}
			default:
				throw new RuntimeException("Entity with strange type requested: " + type);
		}
		
		Entity e;
		if (l.isEmpty()) {
			e = new Entity();
			e.entityType = type;
			add.add(e);
		} else
			e = l.remove(0);
		
		e.isActive = true;
		
		return e;
	}
}
