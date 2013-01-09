package efruchter.tp.trait.generators;

import java.awt.Color;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.defaults.ClientDefaults;
import efruchter.tp.defaults.CollisionLabel;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.gui.VectorEditorPopup;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.learning.database.Database;
import efruchter.tp.state.ClientStateManager;
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

    private GeneCurve chainProb, chainDelay, probChainCont, enemySize, enemyHealth;
    private Gene intensity;

    final public static Random random = new Random();

    public long waveCount;

    public LevelGeneratorCore() {
        super("Level Generator : Spawner", "The level generating structure.");
        waveCount = 0;
        chains = new LinkedList<Chain>();
    }

    @Override
    public void onStart(final Entity self, final Level level) {

        if (level.getGeneratorCore().getWaveCount() > 0) {
            if (TraitProjectClient.PREFERENCES.get("username", null) == null) {
                final String username = JOptionPane.showInputDialog("Please enter a username:");
                if (username != null) {
                    TraitProjectClient.PREFERENCES.put("username", username);
                }
            }
            GeneVectorIO.storeVector(
                    new Database.SessionInfo(TraitProjectClient.PREFERENCES.get("username", "NO_NAME"), Long.toString(TraitProjectClient
                            .getScore()), new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())),
                    GeneVectorIO.getExplorationVector());

        }

        waveCount++;
        time = 0;
        TraitProjectClient.setScore(0);
        chains.clear();

        /*
         * Takes care of the case where the GUI has already loaded the vector.
         */
        if (waveCount > 1)
            GeneVectorIO.reloadExplorationVector();

        /*
         * Build the gene vectors over again.
         */

        intensity = GeneVectorIO.getExplorationVector().storeGene("spawner.intensity",
                new Gene("Intensity", "Intensity of everything.", 0, 1, 1f / 2f), false);

        chainProb = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.newChainProb",
                new GeneCurve("newChainProb", "P(new chain)", 0, 1, 0), false);
        chainProb.genes[0].setValue(.02f);
        chainProb.genes[1].setValue(.02f);
        chainProb.genes[2].setValue(.05f);
        chainProb.genes[3].setValue(.05f);

        chainDelay = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.chainDelay",
                new GeneCurve("chainDelay", "Delay until enemy is spawned to continue a chain.", 0, 1000, 500), false);
        probChainCont = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.probChainCont",
                new GeneCurve("probChainCont", "P(continue chain)", 0, 1, .90f), false);

        enemySize = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.enemy.radius",
                new GeneCurve("baseRadius", "Base enemy radius.", 2, 50, 15), false);
        enemyHealth = GeneVectorIO.getExplorationVector().storeGeneCurve("spawner.enemy.health",
                new GeneCurve("enemyHealth", "Default enemy health on spawn.", 2, 100, 10), false);

        /*
         * Canned player position.
         */
        final float playerX, playerY;
        if (level.getPlayer() != null) {
            playerX = level.getPlayer().x;
            playerY = level.getPlayer().y;
            // level.removeEntity(level.getPlayer());
        } else {
            playerX = Display.getWidth() / 2;
            playerY = Display.getHeight() * .15f;
        }

        for (final Entity ship : level.getEntities(EntityType.SHIP)) {
            if (ship.isActive())
                level.removeEntity(ship);
        }
        for (final Entity proj : level.getEntities(EntityType.PROJECTILE)) {
            if (proj.isActive())
                level.removeEntity(proj);
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

        // Add the new wave animation
        EntityFactory.buildNewWaveAnim(level.getBlankEntity(EntityType.BG));

        ClientStateManager.setPaused(true);
        VectorEditorPopup.show(TraitProjectClient.getPlayerControlledGenes(), false, "An enemy wave is attacking! Customize your ship!");
    }

    @Override
    public void onUpdate(final Entity self, final Level level, final long delta) {

        time += delta;

        if (time > ClientDefaults.LEVEL_LENGTH || level.getPlayer() == null) {
            onStart(self, level);
        }

        final float mu = (float) time / ClientDefaults.LEVEL_LENGTH;
        final float randNum = (float) Math.random();

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

                // Pathing
                final BehaviorChain c = new BehaviorChain(false);
                c.addBehavior(CurveInterpolator.buildPath(12000, false, curve), 12000);
                c.addBehavior(new KillBehavior(), 1);
                e.addTrait(c);

                // attacking
                final BehaviorChain a = new BehaviorChain(true);
                a.addBehavior(Behavior.EMPTY, 1000);
                a.addBehavior(new BasicAttackTrait(tracking), 500);
                e.addTrait(a);

                // Score adder
                final TraitAdapter kill = new TraitAdapter("", "") {
                    @Override
                    public void onUpdate(final Entity self, final Level level, final long delta) {
                        if (self.health < 0) {
                            TraitProjectClient.setScore(TraitProjectClient.getScore() + ClientDefaults.SCORE1_ENEMY_DEFEAT);
                        }
                    }
                };
                e.addTrait(kill);

                return e;
            }

            private Point.Float[] curve;
            private boolean tracking;
            private float radius, health;

            @Override
            public void precalc(final Level level, final float mu) {

                curve = new Point.Float[3];

                switch (random.nextInt(5)) {
                    case 0:
                        curve[0] = new Point.Float(Display.getWidth() + 20, Display.getHeight() - Display.getHeight() * random.nextFloat()
                                * .25f);
                        break;
                    case 1:
                        curve[0] = new Point.Float(-20, Display.getHeight() - Display.getHeight() * random.nextFloat() * .25f);
                        break;
                    default:
                        curve[0] = new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() + 20);
                }

                curve[1] = new Point.Float(Display.getWidth() * random.nextFloat(), Display.getHeight() - Display.getHeight()
                        * random.nextFloat() * .75f);

                switch (random.nextInt(5)) {
                    case 0:
                        curve[2] = new Point.Float(Display.getWidth() + 20, Display.getHeight() - Display.getHeight() * random.nextFloat()
                                * .25f);
                        break;
                    case 1:
                        curve[2] = new Point.Float(-20, Display.getHeight() - Display.getHeight() * random.nextFloat() * .25f);
                        break;
                    default:
                        curve[2] = new Point.Float(Display.getWidth() * random.nextFloat(), -20);
                }

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

    public long getWaveCount() {
        return waveCount;
    }

    public void resetWaveCount() {
        waveCount = 0;
    }

    public long getTime() {
        return time;
    }
}
