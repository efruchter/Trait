package efruchter.tp.trait.gene;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;

/**
 * Interpolator for a gene. Transitions it between two expression values over a
 * length of time.
 * 
 * @author toriscope
 * 
 */
public class GeneExpressionInterpolator extends Trait {
	
	private Gene g;
	private float startExp;
	private float blendExp;
	private long blendTime;
	private long currTime = 0;
	
	/**
	 * Create an interpolator trait.
	 * 
	 * @param g
	 *            the gene to expression blend
	 * @param startExp
	 *            starting expression
	 * @param endExp
	 *            ending expression
	 * @param blendTime
	 *            the total blendTime
	 */
	public GeneExpressionInterpolator(final Gene g, final float startExp, final float endExp, final long blendTime) {
		super(g.getName() + " Blender", g.getInfo());
		this.g = g;
		this.startExp = startExp;
		this.blendExp = endExp - startExp;
		this.blendTime = blendTime;
		currTime = 0;
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		currTime = 0;
		g.setExpression(startExp);
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		if (currTime >= blendTime)
			g.setExpression(startExp + blendExp);
		else {
			g.setExpression(startExp + ((float) currTime / blendTime) * blendExp);
		}
		
		currTime += delta;
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
