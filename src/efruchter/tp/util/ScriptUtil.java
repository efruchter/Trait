package efruchter.tp.util;

import efruchter.tp.entities.Ship;

public class ScriptUtil {

	private ScriptUtil() {
	}

	public boolean isColliding(Ship a, Ship b) {
		return isNear(a, b.x, b.y, b.radius);
	}

	public boolean isNear(Ship a, float x, float y, float radius) {
		float dx, dy;
		dx = a.x - x;
		dy = a.y - y;
		if (((a.radius + radius) * (a.radius + radius)) > (dx * dx) + (dy * dy))
			return true;
		else
			return false;
	}
}
