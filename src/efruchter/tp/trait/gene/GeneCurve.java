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
        genes = new Gene[4];

        this.name = name;
        this.info = info;

        for (int i = 0; i < genes.length; i++) {
            genes[i] = new Gene("c1", info, minVal, maxVal, initialVal);
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
        return MathUtil.cubicInterpolate(genes[0].getValue(), genes[1].getValue(), genes[2].getValue(), genes[3].getValue(), mu);
    }

}
