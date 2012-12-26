package efruchter.tp.trait.behavior.custom;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.util.MathUtil;

public class MoveBehavior implements Behavior {

	private final float dx, dy, scale;

	public MoveBehavior(final float dx, final float dy, final float scale, final boolean normalize) {
		final float mult = normalize ? MathUtil.invSqrt(dx * dx + dy * dy) : 1;
		this.dx = dx * mult;
		this.dy = dy * mult;
		this.scale = scale;
	}

	public MoveBehavior(final float dx, final float dy, final float scale) {
		this(dx, dy, scale, false);
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.x += dx * scale;
		self.y += dy * scale;
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}
}
