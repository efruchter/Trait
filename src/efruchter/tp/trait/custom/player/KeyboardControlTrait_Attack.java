package efruchter.tp.trait.custom.player;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import efruchter.tp.defaults.CollisionLabel;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.trait.MoveTrait;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.custom.DieOffScreenTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TimedDeathTrait;
import efruchter.tp.trait.custom.WiggleTrait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;
import efruchter.tp.util.KeyUtil;

/**
 * Govern the attack of an entity with the keyboard.
 * 
 * @author toriscope
 * 
 */
public class KeyboardControlTrait_Attack extends Trait {

	private float cd;
	public final Gene coolDown, spread, wiggleBigness, amount, damage, dx, dy;

	/**
	 * Create standard attack controller.
	 */
	public KeyboardControlTrait_Attack() {
		super("Attack Control", "Entity attack linked to keyboard inputs.");
		coolDown = GeneVectorIO.getExplorationVector().storeGene("player.attack.cooldown",
		        new Gene("Cool Down Delay", "The projectile cooldown.", 0, 1000, 64), false);
		spread = GeneVectorIO.getExplorationVector().storeGene("player.attack.spread",
		        new Gene("Launch Spread", "Bullet spread.", 0, 1, 0), false);
		wiggleBigness = GeneVectorIO.getExplorationVector().storeGene("player.attack.wiggle",
		        new Gene("Wiggle", "Maximum wiggle magnitude.", 0, 1, .5f), false);
		amount = GeneVectorIO.getExplorationVector().storeGene("player.attack.amount",
		        new Gene("# of Bullets", "Amount of bullets per salvo.", 0, 10, 1), false);
		damage = GeneVectorIO.getExplorationVector().storeGene("player.attack.damage",
		        new Gene("Damage Per Bullet", "Amount of damage per bullet.", 0, 10, 5), false);
		dx = GeneVectorIO.getExplorationVector().storeGene("player.attack.dx", new Gene("dx", "dx travel per step", -1, 1, 0),
		        false);
		dy = GeneVectorIO.getExplorationVector().storeGene("player.attack.dy", new Gene("dy", "dy travel per step", -1, 1, 1),
		        false);
	}

	@Override
	public void onStart(final Entity self, final Level level) {

	}

	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {

		if (cd < coolDown.getValue()) {
			cd += delta;
		}
		if (cd >= coolDown.getValue()) {
			if (KeyUtil.isKeyDown(Keyboard.KEY_SPACE)) {
				cd = 0;
				for (int i = 0; i < amount.getValue(); i++) {
					final Entity p = level.getBlankEntity(EntityType.PROJECTILE);
					EntityFactory.buildProjectile(p, self.x, self.y, 4, CollisionLabel.PLAYER_LABEL, Color.GREEN, damage.getValue());
					p.addTrait(new DieOffScreenTrait());
					p.addTrait(new TimedDeathTrait(10));

					final WiggleTrait w = new WiggleTrait(20);
					w.wiggleChance.setExpression(1);
					w.wiggleIntensity.setExpression(wiggleBigness.getExpression());

					p.addTrait(w);

					final float x = dx.getValue() + (Math.random() < .5 ? -1 : 1) * spread.getValue() * (float) Math.random();
					final float y = dy.getValue();

					final MoveTrait t = new MoveTrait(x, y, 1f);

					p.addTrait(t);

					final RadiusEditTrait rad = new RadiusEditTrait(3, 10, 10);
					p.addTrait(rad);

					p.addTrait(new GeneExpressionInterpolator(rad.radius, 0, 1, 200));
				}
			}
		}
	}
	@Override
	public void onDeath(final Entity self, final Level level) {

	}

}
