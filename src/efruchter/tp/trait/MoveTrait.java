package efruchter.tp.trait;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.util.MathUtil;

public class MoveTrait extends Trait {

	private final float dx, dy, scale;

	public MoveTrait(final float dx, final float dy, final float scale, final boolean normalize) {
		super("Move Trait", "");
		final float mult = normalize ? MathUtil.invSqrt(dx * dx + dy * dy) : 1;
		this.dx = dx * mult;
		this.dy = dy * mult;
		this.scale = scale;
	}

	public MoveTrait(final float dx, final float dy, final float scale) {
		this(dx, dy, scale, false);
	}

	@Override
	public void onStart(Entity self, Level level) {

	}

	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.x += dx * scale * delta;
		self.y += dy * scale * delta;
	}

	@Override
	public void onDeath(Entity self, Level level) {

	}
}
