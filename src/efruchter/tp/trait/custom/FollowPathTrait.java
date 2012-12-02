package efruchter.tp.trait.custom;

import java.awt.Point;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

public class FollowPathTrait extends Trait {
	
	public boolean smooth;
	public Point.Float[] points;
	private long currTime;
	public long duration;
	
	public FollowPathTrait(long duration, Point.Float... points) {
		super("Follow Path", "Follow a designated path");
		this.smooth = false;
		this.points = points;
		this.duration = duration;
		this.currTime = 0;
		setActive(false);
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		currTime = 0;
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		currTime += delta;
		long pos = currTime = (duration / points.length);
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
