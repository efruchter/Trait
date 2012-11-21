package efruchter.tp.traits;

public class Gene {

	private float exp, maxVal, minVal, val;
	private String name, info;

	public Gene(String name, String info, float minVal, float maxVal,
			float initialVal) {
		this.name = name;
		this.info = info;
		this.minVal = minVal;
		this.maxVal = maxVal;
		setValue(initialVal);

	}

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
