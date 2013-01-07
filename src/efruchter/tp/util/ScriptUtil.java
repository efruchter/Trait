package efruchter.tp.util;

import efruchter.tp.entity.Entity;

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
	 * Detect if two ships are colliding. Both must be active.
	 * 
	 * @param a
	 *            ship 1
	 * @param b
	 *            ship 2
	 * @return true if overlapping, false otherwise.
	 */
	public static boolean isColliding(final Entity a, final Entity b) {
	    if (!a.isActive() || !b.isActive())
	        return false;
	    else
	        return isWithin(a, b.x, b.y, b.radius);
	}
	
	public static boolean isWithin(final Entity a, final float x, final float y, final float radius) {
		final float dx, dy;
		dx = a.x - x;
		dy = a.y - y;
		if (((a.radius + radius) * (a.radius + radius)) > (dx * dx) + (dy * dy))
			return true;
		else
			return false;
	}
}
