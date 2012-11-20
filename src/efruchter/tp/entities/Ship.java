package efruchter.tp.entities;

import java.util.ArrayList;
import java.util.List;

import efruchter.tp.traits.Trait;


public class Ship {

	public float x, y, radius, health;
	public String name = "Ship";

	private Behavior renderBehavior;
	private List<Trait> traits;

	public Ship() {
		x = y = radius = health = 0;
		setRenderBehavior(Behavior.EMPTY);
		traits = new ArrayList<Trait>();
	}

	public void onStart() {
		for (Behavior b : traits) {
			b.onStart(this);
		}
	}

	public void onUpdate(long delta) {
		for (Behavior b : traits) {
			b.onUpdate(this, delta);
		}
	}

	public void onDeath() {
		for (Behavior b : traits) {
			b.onDeath(this);
		}
	}

	public void addTrait(Trait trait) {
		traits.add(trait);
		trait.onStart(this);
	}

	public List<Trait> getTraits() {
		return traits;
	}

	public Behavior getRenderBehavior() {
		return renderBehavior;
	}

	public void setRenderBehavior(Behavior renderBehavior) {
		this.renderBehavior = renderBehavior;
	}
}
