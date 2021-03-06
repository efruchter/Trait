package efruchter.tp;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;




import efruchter.tp.entity.CollisionLabel;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.EntityFactory;
import efruchter.tp.entity.EntityType;
import efruchter.tp.entity.Level;
import efruchter.tp.entity.PolarityController;
import efruchter.tp.gui_broken.VectorEditorPopup_Crummy;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.behavior.custom.KillBehavior;
import efruchter.tp.trait.custom.ConstantHealthBoostTrait;
import efruchter.tp.trait.custom.CurveInterpolator;
import efruchter.tp.trait.custom.LoopScreenTrait;
import efruchter.tp.trait.custom.enemy.BasicAttackTrait;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Attack;
import efruchter.tp.trait.custom.player.KeyboardControlTrait_Movement;
import efruchter.tp.trait.custom.player.PlayerRadiusEditTrait;
import efruchter.tp.trait.custom.player.SetPlayerTrait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneCurve;

/**
 * Not really sure how to approach this. Trying some stuff.
 * 
 * @author toriscope
 * 
 */
public class LevelGeneratorCore extends Trait {

    private long time = 0;

    // Chance of a new chain forming
    final private List<Chain> chains;

    private GeneCurve chainProb;
    private GeneCurve chainDelay;
    private GeneCurve probChainCont;
    private GeneCurve enemySize;
    private GeneCurve enemyHealth;
    private GeneCurve enemyBigness;
    private GeneCurve enemyRouteDuration;
    private Gene intensity;
    private Gene polarityAmount;

    final public static Random random = new Random(0);

    public long waveCount;
    private int percentComplete;

    public LevelGeneratorCore() {
        super("Level Generator : Spawner", "The level generating structure.");
        waveCount = 0;
        chains = new LinkedList<Chain>();
    }

    @Override
    public void onStart(final Entity self, final Level level) {

    	random.setSeed(0);
    	
    	ServerIO v = ClientDefaults.server();
    	
        if (waveCount > 0) {
            SessionInfo info = new SessionInfo();
            info.put("username", Long.toString(TraitProjectClient.playerID));
            info.put("date", Long.toString(System.currentTimeMillis()));
            info.put("vector", v.getExplorationVector().toDataString());
            info.put("s_wave", Long.toString(waveCount));
            info.put("s_damage_player", Float.toString(TraitProjectClient.s_damage_player));
            info.put("s_damage_enemies", Float.toString(TraitProjectClient.s_damage_enemies));
            info.put("s_num_enemies", Float.toString(TraitProjectClient.s_num_enemies));
            info.put("s_fired_player", Float.toString(TraitProjectClient.s_fired_player));
            info.put("s_fired_enemies", Float.toString(TraitProjectClient.s_fired_enemies));
            info.put("s_killed_enemies", Float.toString(TraitProjectClient.s_killed_enemies));
            v.storeInfo(info);
        }

        waveCount++;

        /*
         * Takes care of the case where the GUI has already loaded the vector.
         */
        if (waveCount > 1) {
            v.reloadExplorationVector();
        }
        
        /*
         * Build the gene vectors over again.
         */

        intensity = v.getExplorationVector().storeGene("spawner.intensity",
                new Gene("Intensity", "Intensity of everything.", 0, 1, 1f / 2f), false);

        chainProb = v.getExplorationVector().storeGeneCurve("spawner.newChainProb",
                new GeneCurve("newChainProb", "P(new chain)", 0, 1, 0), false);
        chainProb.genes[0].setValue(.02f);
        chainProb.genes[1].setValue(.05f);

        chainDelay = v.getExplorationVector().storeGeneCurve("spawner.chainDelay",
                new GeneCurve("chainDelay", "Delay until enemy is spawned to continue a chain.", 0, 1000, 500), false);
        probChainCont = v.getExplorationVector().storeGeneCurve("spawner.probChainCont",
                new GeneCurve("probChainCont", "P(continue chain)", 0, 1, .90f), false);

        enemySize = v.getExplorationVector().storeGeneCurve("spawner.enemy.radius",
                new GeneCurve("baseRadius", "Base enemy radius.", 2, 50, 15), false);
        enemyHealth = v.getExplorationVector().storeGeneCurve("spawner.enemy.health",
                new GeneCurve("enemyHealth", "Default enemy health on spawn.", 2, 100, 10), false);
        enemyBigness = v.getExplorationVector().storeGeneCurve("spawner.enemy.bigness",
                new GeneCurve("enemyBigness", "Additional bigness/toughness of enemy. Effects everything.", 4, 0, 100, 0), false);
        enemyBigness.setValues(0, 0f, .05f, .10f);
        
        polarityAmount = v.getExplorationVector().storeGene("spawner.polarity",
                new Gene("polarity", "Amount of possible poles.", 0, PolarityController.COLORS.length, 0), false);
        
        enemyRouteDuration = v.getExplorationVector().storeGeneCurve("spawner.enemy.routeDuration",
                new GeneCurve("routeDuration", "time taken for enemies traverse their routes", 16, 1112000, 12000), false);
        /*
         * Canned player position.
         */
        final float playerX, playerY;
        if (level.getPlayer() != null) {
            playerX = level.getPlayer().x;
            playerY = level.getPlayer().y;
            // level.removeEntity(level.getPlayer());
        } else {
            playerX = TraitProjectClient.SIZE.width / 2;
            playerY = TraitProjectClient.SIZE.height * .15f;
        }

        for (final Entity ship : level.getEntities(EntityType.SHIP)) {
            if (ship.isActive()) {
                //ship.setHealth(-1);
                level.removeEntity(ship);
            }
        }
        for (final Entity proj : level.getEntities(EntityType.PROJECTILE)) {
            if (proj.isActive()) {
                proj.setHealth(-1);
                level.removeEntity(proj);
            }
        }

        // Build Player
        final Entity player = level.getBlankEntity(EntityType.SHIP);
        EntityFactory.buildShip(player, playerX, playerY, 10, CollisionLabel.PLAYER_LABEL, Color.CYAN, 25f);
        // Add control traits to player with arrow-keys
        player.addTrait(new KeyboardControlTrait_Movement());

        player.addTrait(new KeyboardControlTrait_Attack());
        // Radius editing trait
        PlayerRadiusEditTrait rad = new PlayerRadiusEditTrait(3, 20, 10);
        player.addTrait(rad);
        player.name = "Player Ship";
        // Add screen loop trait
        player.addTrait(new LoopScreenTrait());
        player.addTrait(new ConstantHealthBoostTrait());
        player.addTrait(new SetPlayerTrait());
        if (Math.round(polarityAmount.getValue()) != 0) {
            player.polarity = 0;
        }
        //final Gene pSwi = polarityAmount;
        /*player.addTrait(new TraitAdapter (){
            @Override
        	public void onUpdate(Entity self, Level level, long delta) {
               if (KeyHolder.get().isPressedThenRelease(KeyEvent.VK_SHIFT)) {
                   self.polarity = (self.polarity + 1) % (int) (Math.round(pSwi.getValue())); 
               }
            }
        });*/

        // Add the new wave animation
        EntityFactory.buildNewWaveAnim(level.getBlankEntity(EntityType.BG));
        
        time = 0;
        chains.clear();
        
        /* Use this to perform actions on level start
         * when ordering is becoming an issue.
         */
      	level.getBlankEntity(EntityType.SERVICE).addTrait(new TraitAdapter(){
			public void onUpdate(Entity self, Level level, long delta) {

				TraitProjectClient.resetMetrics();

				level.removeEntity(self);
			}
		});

       if (VectorEditorPopup_Crummy.isVisible()) {
    	   VectorEditorPopup_Crummy.hide();
       }
  
       List<GeneWrapper> ge = TraitProjectClient.getPlayerControlledGenes();
	    if (!ge.isEmpty()) {
	    	VectorEditorPopup_Crummy.show(ge, true, "Get ready for the next wave!");
	    }
    }

    @Override
    public void onUpdate(final Entity self, final Level level, final long delta) {
        
        time += delta;
        
        percentComplete = (int) (((float) time / ClientDefaults.levelLength()) * 100);

        if (time > ClientDefaults.levelLength() || level.getPlayer() == null) {
            onStart(self, level);
        }

        final float mu = (float) time / ClientDefaults.levelLength();
        final float randNum = random.nextFloat();

        // Gen
        final float probNewChain = chainProb.getValue(mu);

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

    private LevelGeneratorCore.Chain.GenFunction getNewChainFunction(final Level level, final float mu) {
        return new LevelGeneratorCore.Chain.GenFunction() {

            public Entity gen(final Level level, final float mu) {
                Entity e = level.getBlankEntity(EntityType.SHIP);
                EntityFactory.buildShip(e, -100f, -100f, radius, CollisionLabel.ENEMY_LABEL, Color.RED, health);

                long duration = (long) (enemyRouteDuration.getValue(mu) + (enemyRouteDuration.getValue(mu) *  (random.nextFloat() - .5f)));
                
                // Pathing
                final BehaviorChain c = new BehaviorChain(false);
                c.addBehavior(CurveInterpolator.buildPath(duration, false, curve), duration);
                c.addBehavior(new KillBehavior(), 1);
                e.addTrait(c);

                // attacking
                final BehaviorChain a = new BehaviorChain(true);
                a.addBehavior(Behavior.EMPTY, 1000);
                a.addBehavior(new BasicAttackTrait(tracking, bigness), 500);
                e.addTrait(a);
                
                e.polarity = polarity;
                
                e.addTrait(new TraitAdapter (){
                	@Override
                	public void onStart(Entity self, Level level) {
                		TraitProjectClient.s_num_enemies++;
                    }
                	
                	@Override
                	public void onDeath(Entity self, Level level) {
                		if (self.health <= 0) {
                			TraitProjectClient.s_killed_enemies++;
                		}
                	}
                });

                return e;
            }

            private Point.Float[] curve;
            private boolean tracking;
            private float radius, health;
            private int polarity;
            float bigness;

            @Override
            public void precalc(final Level level, final float mu) {

                if (polarityAmount.getRoundedValue() != 0) {
                    polarity = random.nextInt(polarityAmount.getRoundedValue());
                } else {
                    polarity = -1;
                }

                curve = new Point.Float[3];

                switch (random.nextInt(5)) {
                    case 0:
                        curve[0] = new Point.Float(TraitProjectClient.SIZE.width + 20, TraitProjectClient.SIZE.height - TraitProjectClient.SIZE.height * random.nextFloat()
                                * .25f);
                        break;
                    case 1:
                        curve[0] = new Point.Float(-20, TraitProjectClient.SIZE.height - TraitProjectClient.SIZE.height * random.nextFloat() * .25f);
                        break;
                    default:
                        curve[0] = new Point.Float(TraitProjectClient.SIZE.width * random.nextFloat(), TraitProjectClient.SIZE.height + 20);
                }

                curve[1] = new Point.Float(TraitProjectClient.SIZE.width * random.nextFloat(), TraitProjectClient.SIZE.height - TraitProjectClient.SIZE.height
                        * random.nextFloat() * .75f);

                switch (random.nextInt(5)) {
                    case 0:
                        curve[2] = new Point.Float(TraitProjectClient.SIZE.width + 20, TraitProjectClient.SIZE.height - TraitProjectClient.SIZE.height * random.nextFloat()
                                * .25f);
                        break;
                    case 1:
                        curve[2] = new Point.Float(-20, TraitProjectClient.SIZE.height - TraitProjectClient.SIZE.height * random.nextFloat() * .25f);
                        break;
                    default:
                        curve[2] = new Point.Float(TraitProjectClient.SIZE.width * random.nextFloat(), -20);
                }

                tracking = random.nextFloat() < intensity.getExpression();
                bigness = enemyBigness.getValue(mu) * random.nextFloat();
                radius = enemySize.getValue(mu) + bigness;
                health = enemyHealth.getValue(mu) + bigness;
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

    public long getWaveCount() {
        return waveCount;
    }

    public void resetWaveCount() {
        waveCount = 0;
    }

    public long getTime() {
        return time;
    }

    public int getPercentComplete() {
        return percentComplete;
    }
}
