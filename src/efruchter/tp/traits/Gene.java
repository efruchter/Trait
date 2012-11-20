package efruchter.tp.traits;

public interface Gene {

	/**
	 * Value from 0 to 1. 0 deactivated the gene, 1 is maximum expression.
	 * 
	 * @param prob
	 */
	public void setExpression(float prob);

	public float getExpression();

	public String getInfo();

	public String getName();

	public class GeneFactory {
		private GeneFactory() {
		}

		public static Gene makeDefaultGene(final String name, final String info) {
			return new Gene() {

				private float exp = .5f;

				@Override
				public void setExpression(float prob) {
					exp = prob;
					if (exp > 1)
						exp = 1;
					else if (exp < 0)
						exp = 0;
				}

				@Override
				public float getExpression() {
					return exp;
				}

				@Override
				public String getInfo() {
					return info;
				}

				@Override
				public String getName() {
					return name;
				}

			};
		}
	}
}
