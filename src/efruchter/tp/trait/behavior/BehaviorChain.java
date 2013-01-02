package efruchter.tp.trait.behavior;

import java.util.ArrayList;
import java.util.List;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * This trait will activate Behaviors in a specified order for their allocated
 * durations. Can be sued to make animations, set action cycles, and other
 * fantastic things.
 * 
 * @author toriscope
 * 
 */
public class BehaviorChain extends Trait {
	
	private final List<Behavior> actions;
	private final List<Long> endings;
	private long currTime;
	private int index;
	private boolean loop;
	
	/**
	 * Create a named behavior chained with descriptive info.
	 * 
	 * @param name
	 *            name of chain.
	 * @param info
	 *            info about chain.
	 * @param loop
	 *            true if chain loops.
	 */
	public BehaviorChain(final String name, final String info, final boolean loop) {
		super(name, info);
		actions = new ArrayList<Behavior>();
		endings = new ArrayList<Long>();
		currTime = index = 0;
		this.loop = loop;
	}
	
	/**
	 * Create a behavior chain with standard name/info.
	 * 
	 * @param loop
	 *            true if chain loops.
	 */
	public BehaviorChain(final boolean loop) {
		this("Behavior Chain", "A chain of behaviors for an entity to perform", loop);
	}
	
	/**
	 * Create a non-looping, standard-named behavior chain.
	 */
	public BehaviorChain() {
		this(false);
	}
	
	/**
	 * Add a wait action to the end of the chain.
	 * 
	 * @param duration
	 *            the length of the wait in milliseconds
	 */
	public void addWait(final long duration) {
		addBehavior(new TraitAdapter("WAIT", "Waiting Period."), duration);
	}
	
	/**
	 * Add a behavior to the end of the chain it to the chain.
	 * 
	 * @param behavior
	 *            the behavior to add.
	 * @param duration
	 *            duration of action in milliseconds.
	 */
	public void addBehavior(final Behavior behavior, final long duration) {
		actions.add(behavior);
		endings.add(duration + (endings.isEmpty() ? 0 : endings.get(endings.size() - 1)));
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		currTime = index = 0;
		for (Behavior a : actions) {
			a.onStart(self, level);
		}
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {

		if (loop && index >= actions.size()) {
			currTime = index = 0;
		}

        long remainingDelta = delta;
		
		while (index < actions.size() && remainingDelta > 0) {
            final Behavior b = actions.get(index);

            final boolean active = (!(b instanceof Trait) || (b instanceof Trait && ((Trait) b).isActive()));
            final boolean inactive = b instanceof Trait && !((Trait) b).isActive();

            final long remaining = Math.min(endings.get(index) - currTime, remainingDelta);
			
			currTime += remaining;
			
			if (active) {
                remainingDelta -= remaining;
				actions.get(index).onUpdate(self, level, remaining);
			}
			if (inactive) {
				currTime = endings.get(index);
			}
			
			if (currTime >= endings.get(index)) {
				index++;
				if (loop && index >= actions.size()) {
					onStart(self, level);
				}
			}
		}
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		for (Behavior a : actions) {
			a.onDeath(self, level);
		}
	}
	
	public List<Behavior> getBehaviors() {
		return actions;
	}
	
	public void removeBehavior(final Behavior editingEntity) {
        final int index = actions.indexOf(editingEntity);
		if (index != -1) {
			currTime = this.index = 0;
			actions.remove(index);

            long time = endings.remove(index);
			
			if (index != 0) {
				time -= endings.get(index - 1);
			}
			for (int i = index; i < endings.size(); i++) {
				endings.set(i, endings.get(i) - time);
			}
		}
	}
	
	@Override
	public String toString() {
		return "(C) " + getName() + " (" + (isActive() ? "ON" : "OFF") + ")";
	}
}
