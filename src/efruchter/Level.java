package efruchter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import efruchter.entities.Ship;
import efruchter.util.RenderUtil;

public class Level {

	private Ship player;
	private List<Ship> ships;

	public Level() {
		player = new Ship();
		player.renderBehavior = RenderUtil.getShipRenderer(Color.RED);

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
			b.renderBehavior.onUpdate(b, 0);
		}
	}
}
