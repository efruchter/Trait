package efruchter.tp.actions;

import java.util.ArrayList;
import java.util.List;

import efruchter.tp.entities.Behavior;
import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.traits.Trait;

/**
 * Trying out stuff...
 * 
 * @author toriscope
 * 
 */
public class ActionChain extends Trait {
	private List<Action> actions;
	private List<Long> endings;
	private long currTime;
	private int index;
	
	public ActionChain() {
		super("Action Chain", "A chain of actions for an entity to perform");
		actions = new ArrayList<Action>();
		endings = new ArrayList<Long>();
		currTime = index = 0;
	}
	
	public void addAction(Action action) {
		actions.add(action);
		endings.add(action.timeSpan() + (endings.isEmpty() ? 0L : endings.get(endings.size() - 1)));
	}
	
	public void addAction(final Behavior action, final long timeSpan) {
		addAction(new Action() {
			
			@Override
			public void onStart(Entity self, Level level) {
				action.onStart(self, level);
			}
			
			@Override
			public void onUpdate(Entity self, Level level, long delta) {
				action.onUpdate(self, level, delta);
			}
			
			@Override
			public void onDeath(Entity self, Level level) {
				action.onDeath(self, level);
			}
			
			@Override
			public long timeSpan() {
				return timeSpan;
			}
		});
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		currTime = index = 0;
		for (Action a : actions) {
			a.onStart(self, level);
		}
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		while (delta > 0 && index < actions.size()) {
			Action a = actions.get(index);
			long remaining = Math.min(endings.get(index) - currTime, delta);
			delta -= remaining;
			currTime += remaining;
			a.onUpdate(self, level, remaining);
			if (currTime >= endings.get(index)) {
				index++;
			}
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
}
