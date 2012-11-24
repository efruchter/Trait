package efruchter.tp.traits;

public class Gene {
	
	private float exp, maxVal, minVal, val;
	private String name, info;
	
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
	public Gene(String name, String info, float minVal, float maxVal, float initialVal) {
		this.name = name;
		this.info = info;
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
	public Gene(String name, String info) {
		this(name, info, 0, 1, .5f);
	}
	
	public String getInfo() {
		return info;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(float val) {
		if (val > maxVal)
			val = maxVal;
		else if (val < minVal)
			val = minVal;
		this.val = val;
		exp = (val - minVal) / (maxVal - minVal);
	}
	
	public void setMinValue(float val) {
		this.minVal = val;
		setValue(this.val);
	}
	
	public void setMaxValue(float val) {
		this.maxVal = val;
		setValue(this.val);
	}
	
	public void setExpression(float prob) {
		if (prob > 1)
			prob = 1;
		else if (prob < 0)
			prob = 0;
		exp = prob;
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
}
