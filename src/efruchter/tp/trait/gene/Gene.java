package efruchter.tp.trait.gene;

public class Gene {

	private float exp, maxVal, minVal, val;
	private final String name, info;

	/**
	 * Build a gene that can be expressed between two values.
	 * 
	 * @param name
	 *            Name of the gene.
	 * @param info
	 *            Light information about the gene.
	 * @param minVal
	 *            Minimum float value of the gene.
	 * @param maxVal
	 *            Maximum float value of the gene
	 * @param initialVal
	 *            Initial float value of the gene
	 */
	public Gene(final String name, final String info, final float minVal, final float maxVal, final float initialVal) {
		this.name = name;
		this.info = info;
		this.minVal = minVal;
		this.maxVal = maxVal;
		setValue(initialVal);

	}

	public Gene(final float minVal, final float maxVal, final float initialVal) {
		this.name = "NO_NAME";
		this.info = "NO_INFO";
		this.minVal = minVal;
		this.maxVal = maxVal;
		setValue(initialVal);

	}
	/**
	 * Build a gene with min/max value of 0/1, and initial value set to .5f.
	 * 
	 * @param name
	 *            Name of the gene.
	 * @param info
	 *            Light information about the gene.
	 */
	public Gene(final String name, final String info) {
		this(name, info, 0, 1, .5f);
	}

	public String getInfo() {
		return info;
	}

	public String getName() {
		return name;
	}

	public void setValue(final float newVal) {
		val = newVal;
		if (val > maxVal)
			val = maxVal;
		else if (val < minVal)
			val = minVal;
		exp = (val - minVal) / (maxVal - minVal);
	}

	public void setMinValue(final float val) {
		this.minVal = val;
		setValue(this.val);
	}

	public void setMaxValue(final float val) {
		this.maxVal = val;
		setValue(this.val);
	}

	public void setExpression(final float prob) {
		exp = prob;
		if (exp > 1)
			exp = 1;
		else if (exp < 0)
			exp = 0;
		val = exp * (maxVal - minVal) + minVal;
	}

	public float getExpression() {
		return exp;
	}

	public float getMinValue() {
		return minVal;
	}

	public float getMaxValue() {
		return maxVal;
	}

	public float getValue() {
		return val;
	}

	@Override
	public String toString() {
		return "(G) " + name;
	}
}
