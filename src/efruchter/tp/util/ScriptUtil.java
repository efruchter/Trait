package efruchter.tp.util;

import efruchter.tp.entities.Entity;

/**
 * Some random helper methods for scripting.
 * 
 * @author toriscope
 * 
 */
public class ScriptUtil {
	
	private ScriptUtil() {
	}
	
	/**
	 * Detect if two ships are colliding.
	 * 
	 * @param a
	 *            ship 1
	 * @param b
	 *            ship 2
	 * @return true if overlapping, false otherwise.
	 */
	public static boolean isColliding(Entity a, Entity b) {
		return isWithin(a, b.x, b.y, b.radius);
	}
	
	public static boolean isWithin(Entity a, float x, float y, float radius) {
		float dx, dy;
		dx = a.x - x;
		dy = a.y - y;
		if (((a.radius + radius) * (a.radius + radius)) > (dx * dx) + (dy * dy))
			return true;
		else
			return false;
	}
}
