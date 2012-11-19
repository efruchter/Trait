package trts.entities;

import java.util.ArrayList;
import java.util.List;

import trts.traits.Trait;

public class Ship {

	public float x, y, radius, health;

	private Behavior renderBehavior;
	private List<Behavior> updateBehaviors;

	public Ship() {
		x = y = radius = health = 0;
		setRenderBehavior(Behavior.EMPTY);
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

	public void addTrait(Trait trait) {
		updateBehaviors.add(trait);
		trait.onStart(this);
	}

	public Behavior getRenderBehavior() {
		return renderBehavior;
	}

	public void setRenderBehavior(Behavior renderBehavior) {
		this.renderBehavior = renderBehavior;
	}
}
