package efruchter.tp.trait.gene;

import efruchter.tp.util.MathUtil;

public class GeneCurve {

	public final Gene[] genes;
	public final String name, info;

	public GeneCurve(final String name, final String info, float minVal, float maxVal, float initialVal) {
		genes = new Gene[4];
		
		this.name = name;
		this.info = info;
		
		for (int i = 0; i < genes.length; i++) {
			genes[i] = new Gene("c1", "", minVal, maxVal, initialVal);
		}
	}

	public float getValue(final float mu) {
		return MathUtil.cubicInterpolate(genes[0].getValue(), genes[1].getValue(), genes[2].getValue(), genes[3].getValue(), mu);
	}

}
