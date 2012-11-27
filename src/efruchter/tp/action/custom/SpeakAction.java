package efruchter.tp.action.custom;

import efruchter.tp.action.Action;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;

/**
 * Simple action that displays some text to the terminal, along with the
 * allocated time-step length.
 * 
 * @author toriscope
 * 
 */
public class SpeakAction implements Action {
	
	private String speak;
	private long duration;
	
	public SpeakAction(String s, long duration) {
		speak = s;
		this.duration = duration;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		System.out.println(speak + " | MS allotted:" + delta);
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
	@Override
	public long timeSpan() {
		return duration;
	}
	
}
