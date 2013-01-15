package efruchter.tp.trait.gene;

import efruchter.tp.util.MathUtil;

/**
 * A gene structure that holds a 4-point interpolating curve to represent the
 * gene's value.
 * 
 * @author toriscope
 * 
 */
public class GeneCurve {

    public final Gene[] genes;
    public final String name, info;

    /**
     * Instantiate curve with all genes given a certain setting.
     * 
     * @param name
     * @param info
     * @param minVal
     * @param maxVal
     * @param initialVal
     */
    public GeneCurve(final String name, final String info, float minVal, float maxVal, float initialVal) {
        this(name, info, 2, minVal, maxVal, initialVal);
    }

    public GeneCurve(final String name, final String info, final int curvePoints, float minVal, float maxVal, float initialVal) {
        genes = new Gene[curvePoints];

        this.name = name;
        this.info = info;

        for (int i = 0; i < genes.length; i++) {
            genes[i] = new Gene("[" + i + "]", info + "[" + i + "]", minVal, maxVal, initialVal);
        }
    }

    /**
     * Get the interpolated value of the curve at mu.
     * 
     * @param mu
     *            (value from 0-1)
     * @return value of curve at mu.
     */
    public float getValue(final float mu) {
        int lowerIndex = (int) ((genes.length - 1) * mu);
        int upperIndex = (lowerIndex < genes.length - 1) ? lowerIndex + 1 : lowerIndex;

        final float x0, x1, x2, x3;
        x1 = genes[lowerIndex].getValue();
        x2 = genes[upperIndex].getValue();

        if (lowerIndex == 0) {
            x0 = x1;
        } else {
            x0 = genes[lowerIndex - 1].getValue();
        }

        if (upperIndex == genes.length - 1) {
            x3 = x2;
        } else {
            x3 = genes[lowerIndex + 1].getValue();
        }

        final float upper = (float) upperIndex / (genes.length - 1), lower = (float) lowerIndex / (genes.length - 1);
        final float trueMu = (mu - lower) / (upper - lower);

        return MathUtil.cubicInterpolate(x0, x1, x2, x3, trueMu);
    }

    public void setValues(final float... vals) {
        if (vals.length != genes.length) {
            throw new RuntimeException("Improper length array.");
        } else {
            for (int i = 0; i < vals.length; i++) {
                genes[i].setExpression(vals[i]);
            }
        }
    }
}
