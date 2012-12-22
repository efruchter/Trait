package efruchter.tp.util;

public class CurveUtil {
	
	/**
	 * Catmull-Rom cubic interpolation
	 * 
	 */
	public static float cubicInterpolate(final float y0, final float y1, final float y2, final float y3, final float mu) {
        final float a0, a1, a2, a3, mu2;
		
		mu2 = mu * mu;
		
		a0 = -0.5f * y0 + 1.5f * y1 - 1.5f * y2 + 0.5f * y3;
		a1 = y0 - 2.5f * y1 + 2f * y2 - 0.5f * y3;
		a2 = -0.5f * y0 + 0.5f * y2;
		a3 = y1;
		
		return (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3);
	}
}
