package efruchter.entities;

import java.util.ArrayList;
import java.util.List;

public class Ship {

	public float x, y, radius, health;

	public Behavior renderBehavior;
	public List<Behavior> updateBehaviors;

	public Ship() {
		x = y = radius = health = 0;
		renderBehavior = Behavior.EMPTY;
		updateBehaviors = new ArrayList<Behavior>();
	}

	public void onStart() {
		for (Behavior b : updateBehaviors) {
			b.onStart(this);
		}
	}

	public void onUpdate(long delta) {
		for (Behavior b : updateBehaviors) {
			b.onUpdate(this, delta);
		}
	}

	public void onDeath() {
		for (Behavior b : updateBehaviors) {
			b.onDeath(this);
		}
	}
}
