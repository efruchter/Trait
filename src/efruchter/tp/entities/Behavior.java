package efruchter.tp.entities;

public interface Behavior {

	public void onStart(Ship self);

	public void onUpdate(Ship self, long delta);

	public void onDeath(Ship self);

	public static Behavior EMPTY = new Behavior() {

		@Override
		public void onStart(Ship self) {

		}

		@Override
		public void onUpdate(Ship self, long delta) {

		}

		@Override
		public void onDeath(Ship self) {

		}
	};
}
