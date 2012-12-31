package efruchter.tp.entity;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import efruchter.tp.defaults.EntityType;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.util.RenderUtil;

/**
 * Stores and coordinates entities.
 * 
 * @author toriscope
 * 
 */
public class Level {

	private final Map<EntityType, ArrayList<Entity>> entities;
	private final Map<EntityType, LinkedList<Entity>> recycle;

	private final List<Entity> add, remove;
	private final List<Behavior> renderBehaviors;

	private Entity player;

	public Level() {
		entities = new HashMap<EntityType, ArrayList<Entity>>();
		recycle = new HashMap<EntityType, LinkedList<Entity>>();

		// Some canned lists
		entities.put(EntityType.SHIP, new ArrayList<Entity>());
		entities.put(EntityType.PROJECTILE, new ArrayList<Entity>());
		entities.put(EntityType.BG, new ArrayList<Entity>());
		entities.put(EntityType.GENERATOR, new ArrayList<Entity>());

		recycle.put(EntityType.SHIP, new LinkedList<Entity>());
		recycle.put(EntityType.PROJECTILE, new LinkedList<Entity>());
		recycle.put(EntityType.BG, new LinkedList<Entity>());

		add = new LinkedList<Entity>();
		remove = new LinkedList<Entity>();

		renderBehaviors = new LinkedList<Behavior>();
	}

	public void addRenderBehavior(final Behavior beh) {
		renderBehaviors.add(beh);
	}

	public void onUpdate(final long delta) {
		for (final Entity b : entities.get(EntityType.PROJECTILE)) {
			b.onUpdate(delta, this);
		}

		for (final Entity b : entities.get(EntityType.SHIP)) {
			b.onUpdate(delta, this);
		}

		for (final Entity b : entities.get(EntityType.BG)) {
			b.onUpdate(delta, this);
		}

		for (final Entity b : entities.get(EntityType.GENERATOR)) {
			b.onUpdate(delta, this);
		}

		maint();
	}

	public void onDeath() {
		for (final Entry<EntityType, ArrayList<Entity>> entry : entities.entrySet()) {
			for (Entity e : entry.getValue()) {
				e.setActive(false);
				e.onDeath(this);
			}
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

	public void renderGL(final long delta) {

		for (final Behavior b : renderBehaviors) {
			b.onUpdate(null, this, delta);
		}

		try {
			for (final Entity b : entities.get(EntityType.BG)) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}

			for (final Entity b : entities.get(EntityType.SHIP)) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}

			for (final Entity b : entities.get(EntityType.PROJECTILE)) {
				b.getRenderBehavior().onUpdate(b, this, delta);
			}

		} catch (final ConcurrentModificationException e) {

		}
	}

	public void removeEntity(Entity p) {
		remove.add(p);
	}

	private void removeEntityUnsafe(Entity p) {

		if (!recycle.containsKey(p.entityType)) {
			recycle.put(p.entityType, new LinkedList<Entity>());
		}

		if (p.isActive()) {
			recycle.get(p.entityType).add(p);
		}

		p.onDeath(this);
		p.reset();
	}

	private void addEntityUnsafe(Entity p) {
		if (!entities.containsKey(p.entityType)) {
			entities.put(p.entityType, new ArrayList<Entity>());
		}
		entities.get(p.entityType).add(p);
	}

	@Override
	public String toString() {
		return "Level";
	}

	public List<Entity> getEntities(EntityType type) {
		return entities.get(type);
	}

	/**
	 * Get a blank entity, automatically added to the level.
	 * 
	 * @return
	 */
	public Entity getBlankEntity(final EntityType type) {
		List<Entity> l;

		if (recycle.containsKey(type)) {
			l = recycle.get(type);
		} else {
			l = null;
		}

		Entity e;
		if (l == null || l.isEmpty()) {
			e = new Entity();
			e.entityType = type;
			add.add(e);
		} else {
			e = l.remove(0);
		}

		e.setActive(true);

		return e;
	}

	public Entity getPlayer() {
		return player;
	}

	public void setPlayer(Entity player) {
		this.player = player;
	}
}
