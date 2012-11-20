package efruchter.tp.entities;

public interface Behavior {

	public void onStart(Entity self, Level level);

	public void onUpdate(Entity self, Level level, long delta);

	public void onDeath(Entity self, Level level);

	public static Behavior EMPTY = new Behavior() {

		@Override
		public void onStart(Entity self, Level level) {

		}

		@Override
		public void onUpdate(Entity self, Level level, long delta) {

		}

		@Override
		public void onDeath(Entity self, Level level) {

		}
	};
}
