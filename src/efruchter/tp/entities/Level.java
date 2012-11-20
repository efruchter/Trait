package efruchter.tp.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import efruchter.tp.util.RenderUtil;

public class Level {

	private Entity player;
	private List<Entity> ships;

	public Level() {
		player = new Ship();
		player.setRenderBehavior(RenderUtil.getShipRenderer(Color.RED));
		player.radius = 10;
		player.x = player.y = 100;

		ships = new ArrayList<Entity>();
		ships.add(player);
	}

	public void onStart() {
		for (Entity b : ships) {
			b.onStart(this);
		}
	}

	public void onUpdate(long delta) {
		for (Entity b : new LinkedList<Entity>(ships)) {
			b.onUpdate(delta, this);
		}
	}

	public void onDeath() {
		for (Entity b : ships) {
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
	}

	public void removeEntity(Entity self) {
		ships.remove(self);
		self.onDeath(this);
	}

	public void addEntity(Projectile p) {
		ships.add(p);
	}

	public List<Entity> getEntities() {
		return ships;
	}
}
