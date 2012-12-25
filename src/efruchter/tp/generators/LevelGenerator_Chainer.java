package efruchter.tp.generators;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;

import efruchter.tp.defaults.CollisionLabel;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.behavior.KillBehavior;
import efruchter.tp.trait.custom.CurveInterpolator;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.util.CurveUtil;

/**
 * Not really sure how to approach this. Trying some stuff.
 * 
 * @author toriscope
 * 
 */
public class LevelGenerator_Chainer extends Trait {

	private long time = 0;

	// Chance of a new chain forming
	final private float[] chainProb;
	final private List<Chain> chains;
	final long LEVEL_LENGTH = 1000;

	final private Gene probChainCont, intensity;
	final private long chaindelay = 500;

	final public static Random random = new Random();

	public LevelGenerator_Chainer() {
		super("Level Generator : Spawner", "");

		chainProb = new float[4];
		chainProb[0] = GeneVectorIO.getExplorationVector().storeGene("spawner.c1", new Gene(0, 1, 0), false).getExpression();
		chainProb[1] = GeneVectorIO.getExplorationVector().storeGene("spawner.c2", new Gene(0, 1, .25f), false).getExpression();
		chainProb[2] = GeneVectorIO.getExplorationVector().storeGene("spawner.c3", new Gene(0, 1, .5f), false).getExpression();
		chainProb[3] = GeneVectorIO.getExplorationVector().storeGene("spawner.c4", new Gene(0, 1, 1), false).getExpression();

		intensity = GeneVectorIO.getExplorationVector().storeGene("spawner.intensity",
		        new Gene("Intensity", "Intensity of everything.", 0, 1, 1f / 10f), false);

		probChainCont = GeneVectorIO.getExplorationVector().storeGene("spawner.probChainCont", new Gene(0, 1, 1f / 5), false);
		chains = new LinkedList<Chain>();
	}

	@Override
	public void onStart(final Entity self, final Level level) {
		time = 0;
		chains.clear();
	}

	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		time += delta;
		final float randNum = (float) Math.random();

		// Gen
		final float newChainProb = CurveUtil.cubicInterpolate(chainProb[0], chainProb[1], chainProb[2], chainProb[3], (float) time
		        / LEVEL_LENGTH);

		// start new chain?
		if (randNum < newChainProb * intensity.getExpression()) {
			Chain c = new Chain(getNewChainFunction());
			chains.add(c);
		}

		// update chains
		for (final Chain chain : new LinkedList<Chain>(chains)) {
			boolean killChain = false;
			chain.remaining -= delta;
			if (chain.remaining <= 0) {
				if (Math.random() < probChainCont.getExpression() * intensity.getExpression()) {
					chain.genFunc.gen(level);
				} else {
					killChain = true;
				}
				chain.remaining = chaindelay;
			}
			if (killChain) {
				chains.remove(chain);
			}
		}
	}

	private efruchter.tp.generators.LevelGenerator_Chainer.Chain.GenFunction getNewChainFunction() {
		return new efruchter.tp.generators.LevelGenerator_Chainer.Chain.GenFunction() {
			public Entity gen(final Level level) {
				Entity e = level.getBlankEntity(EntityType.SHIP);
				EntityFactory.buildShip(e, -100, -100, 20, CollisionLabel.ENEMY_LABEL, Color.RED, 15);

				// Pathing
				BehaviorChain c = new BehaviorChain(false);
				c.addBehavior(CurveInterpolator.buildPath(10000, false,
				        new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() + e.radius),
				        new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() * random.nextFloat()),
				        new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() * random.nextFloat()),
				        new Point.Float(Display.getWidth() * random.nextFloat(), -e.radius)), 10000);
				c.addBehavior(new KillBehavior(), 1);
				e.addTrait(c);

				return e;
			}
		};
	}

	@Override
	public void onDeath(Entity self, Level level) {
		// TODO Auto-generated method stub

	}

	private static class Chain {
		final private GenFunction genFunc;
		private long remaining = 0;
		public Chain(final GenFunction genFunc) {
			this.genFunc = genFunc;
		}

		private static interface GenFunction {
			Entity gen(final Level level);
		}
	}
}
