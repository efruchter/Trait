package efruchter.tp.trait.custom;

import java.awt.Point;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.util.CurveUtil;

/**
 * Cubic-spline interpolation between two points. (Catmull-Rom).
 * 
 * @author toriscope
 * 
 */
public class CurveInterpolator extends Trait {

	private long currTime;
	private final long duration;
	private final Point.Float x0, x1, x2, x3;
	
	/**
	 * Create an interpolator that interpolates between two points, x1 and x2.
	 * 
	 * @param duration
	 *            the millisecond duration of the interpolation.
	 * @param x0
	 *            x - 1
	 * @param x1
	 *            x
	 * @param x2
	 *            x + 1
	 * @param x3
	 *            x + 2
	 */
	public CurveInterpolator(final long duration, final Point.Float x0, final Point.Float x1, final Point.Float x2, final Point.Float x3) {
		super("Follow Path", "Follow a designated path");
		this.duration = duration;
		this.currTime = 0;
		
		this.x0 = x0;
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		currTime = 0;
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		currTime += delta;
		
		self.x = CurveUtil.cubicInterpolate(x0.x, x1.x, x2.x, x3.x, (float) currTime / duration);
		self.y = CurveUtil.cubicInterpolate(x0.y, x1.y, x2.y, x3.y, (float) currTime / duration);
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
	/**
	 * Build a chain of interpolators to form a curved path.
	 * 
	 * @param duration
	 *            millisecond duration
	 * @param loop
	 *            true to form a closed loop
	 * @param points
	 *            the points along the path
	 * @return the behvaior chain filled with interpolation behaviors.
	 */
	public static BehaviorChain buildPath(final long duration, final boolean loop, final Point.Float... points) {
        final BehaviorChain ps = new BehaviorChain(loop);

        final long dur = duration / (points.length + (loop ? 1 : 0));
		
		for (int i = 0; i < points.length - 1; i++) {

            final Point.Float x0, x1, x2, x3;
			
			if (i == 0) {
				if (loop) {
					x0 = points[points.length - 1];
				} else {
					x0 = points[0];
				}
			} else {
				x0 = points[i - 1];
			}
			
			x1 = points[i];
			x2 = points[i + 1];
			
			if (i == points.length - 2) {
				if (loop) {
					x3 = points[0];
				} else {
					x3 = points[i + 1];
				}
			} else {
				x3 = points[i + 2];
			}
			
			ps.addBehavior(new CurveInterpolator(dur, x0, x1, x2, x3), dur);
		}
		
		if (loop) {
			ps.addBehavior(new CurveInterpolator(dur, points[points.length - 2], points[points.length - 1], points[0],
					points[1]), dur);
		}
		
		return ps;
	}
}
