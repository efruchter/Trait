package efruchter.tp.trait.generators;

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
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.behavior.custom.KillBehavior;
import efruchter.tp.trait.custom.CurveInterpolator;
import efruchter.tp.trait.custom.enemy.BasicAttackTrait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneCurve;

/**
 * Not really sure how to approach this. Trying some stuff.
 * 
 * @author toriscope
 * 
 */
public class LevelGenerator_Chainer extends Trait {

	public long time = 0;

	// Chance of a new chain forming
	final private List<Chain> chains;
	final public long LEVEL_LENGTH = 60000;

	final public GeneCurve chainProb, chainDelay, probChainCont, enemySize, enemyHealth;
	final public Gene intensity;
	
	final public static Random random = new Random();
	public float probNewChain = 0;

	public LevelGenerator_Chainer() {
		super("Level Generator : Spawner", "");

		intensity = GeneVectorIO.getExplorationVector().storeGene("spawner.intensity", new Gene("Intensity", "Intensity of everything.", 0, 1, 1f / 2f), false);
		
		chainProb = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.newChainProb", new GeneCurve("newChainProb", "P(new chain)", 0, 1, 0), false);
		{
			chainProb.genes[0].setValue(0f);
			chainProb.genes[1].setValue(0f);
			chainProb.genes[2].setValue(.15f);
			chainProb.genes[3].setValue(.15f);
		}
		
		chainDelay = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.chainDelay", new GeneCurve("chainDelay", "Delay until enemy is spawned to continue a chain.", 0, 1000, 500), false);
		probChainCont = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.probChainCont", new GeneCurve("probChainCont", "P(continue chain)", 0, 1, .90f), false);
		
		chains = new LinkedList<Chain>();
		
		enemySize = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.enemy.radius", new GeneCurve("baseRadius", "Base enemy radius.", 2, 50, 15), false);
		enemyHealth = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.enemy.health", new GeneCurve("enemyHealth", "Default enemy health on spawn.", 2, 100, 10), false);
		
	}

	@Override
	public void onStart(final Entity self, final Level level) {
		time = 0;
		chains.clear();
		System.out.println("Level generator rebooted.");
	}

	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		time += delta;

		if (time > LEVEL_LENGTH) {
			onStart(self, level);
		}
		
		final float mu = (float) time / LEVEL_LENGTH;
		final float randNum = (float) Math.random();

		// Gen
		probNewChain = chainProb.getValue(mu);

		// start new chain?
		if (randNum < probNewChain * intensity.getExpression()) {
			Chain c = new Chain(getNewChainFunction(level, mu), level, mu);
			chains.add(c);
		}

		// update chains
		for (final Chain chain : new LinkedList<Chain>(chains)) {
			boolean killChain = false;
			chain.remaining -= delta;
			if (chain.remaining <= 0) {
				if (random.nextFloat() < probChainCont.getValue(mu) * intensity.getExpression()) {
					chain.genFunc.gen(level, mu);
				} else {
					killChain = true;
				}
				chain.remaining = (long) chainDelay.getValue(mu);
			}
			if (killChain) {
				chains.remove(chain);
			}
		}
	}

	private efruchter.tp.trait.generators.LevelGenerator_Chainer.Chain.GenFunction getNewChainFunction(final Level level, final float mu) {
		return new efruchter.tp.trait.generators.LevelGenerator_Chainer.Chain.GenFunction() {

			public Entity gen(final Level level, final float mu) {
				Entity e = level.getBlankEntity(EntityType.SHIP);
				EntityFactory.buildShip(e, -100f, -100f, radius, CollisionLabel.ENEMY_LABEL, Color.RED, health);

				// Pathing
				final BehaviorChain c = new BehaviorChain(false);
				c.addBehavior(CurveInterpolator.buildPath(10000, false, curve), 10000);
				c.addBehavior(new KillBehavior(), 1);
				e.addTrait(c);

				// attacking
				final BehaviorChain a = new BehaviorChain(true);
				a.addBehavior(Behavior.EMPTY, 1000);
				a.addBehavior(new BasicAttackTrait(tracking), 500);
				e.addTrait(a);

				return e;
			}

			private Point.Float[] curve;
			private boolean tracking;
			private float radius, health;

			@Override
			public void precalc(final Level level, final float mu) {
				curve = new Point.Float[]{new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() + 20),
				        new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() - Display.getHeight() * random.nextFloat() * .25f),
				        new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() * random.nextFloat() * .75f),
				        new Point.Float(Display.getWidth() * random.nextFloat(), -20)};
				tracking = random.nextFloat() < intensity.getExpression();
				radius = enemySize.getValue(mu);
				health = enemyHealth.getValue(mu);
			}
		};
	}
	@Override
	public void onDeath(Entity self, Level level) {
		
	}

	private static class Chain {
		final private GenFunction genFunc;
		private long remaining = 0;
		public Chain(final GenFunction genFunc, final Level level, final float mu) {
			this.genFunc = genFunc;
			genFunc.precalc(level, mu);
		}

		private static interface GenFunction {
			void precalc(final Level level, final float mu);
			Entity gen(final Level level, final float mu);
		}
	}
}
