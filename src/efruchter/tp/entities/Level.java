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
	
	private Ship player;
	private List<Ship> ships;
	private List<Projectile> bullets;
	
	public Level() {
		player = new Ship();
		player.radius = 10;
		player.x = player.y = 100;
		
		ships = new LinkedList<Ship>();
		ships.add(player);
		
		bullets = new LinkedList<Projectile>();
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
	
	public Entity getPlayer() {
		return player;
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
		if (p instanceof Ship)
			ships.remove((Ship) p);
		else if (p instanceof Projectile)
			bullets.remove((Projectile) p);
		p.onDeath(this);
	}
	
	public void addEntity(Entity p) {
		if (p instanceof Ship)
			ships.add((Ship) p);
		else if (p instanceof Projectile)
			bullets.add((Projectile) p);
		p.onStart(this);
	}
	
	public List<Ship> getShips() {
		return ships;
	}
	
	public List<Projectile> getBullets() {
		return bullets;
	}
	
	public int getEntityCount() {
		return ships.size() + bullets.size();
	}
}
