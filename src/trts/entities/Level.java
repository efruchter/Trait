package trts.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import trts.util.RenderUtil;

public class Level {

	private Ship player;
	private List<Ship> ships;

	public Level() {
		player = new Ship();
		player.setRenderBehavior(RenderUtil.getShipRenderer(Color.RED));
		player.radius = 10;
		player.x = player.y = 100;

		ships = new ArrayList<Ship>();
		ships.add(player);
	}

	public void onStart() {
		for (Ship b : ships) {
			b.onStart();
		}
	}

	public void onUpdate(long delta) {
		for (Ship b : ships) {
			b.onUpdate(delta);
		}
	}

	public void onDeath() {
		for (Ship b : ships) {
			b.onDeath();
		}
	}

	public Ship getPlayer() {
		return player;
	}

	public void renderGL() {
		for (Ship b : ships) {
			b.getRenderBehavior().onUpdate(b, 0);
		}
	}
}
