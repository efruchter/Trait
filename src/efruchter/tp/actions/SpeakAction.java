package efruchter.tp.actions;

import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;


public class SpeakAction implements Action {
	
	private String speak;
	
	public SpeakAction(String s) {
		this.speak = s;
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
		return 1000;
	}
	
}
