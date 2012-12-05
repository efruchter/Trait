package efruchter.tp.trait.custom.player;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.custom.DieOffScreenTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TimedDeathTrait;
import efruchter.tp.trait.custom.TravelSimple;
import efruchter.tp.trait.custom.WiggleTrait;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;

/**
 * Govern the attack of an entity with the keyboard.
 * 
 * @author toriscope
 * 
 */
public class KeyboardControlTrait_Attack extends Trait {
	
	private float cd;
	public Gene coolDown, spread, wiggleBigness, amount, damage, dx, dy;
	private int key;
	
	/**
	 * Create standard attack controller.
	 * 
	 * @param keyChar
	 *            character to launch salvo.
	 */
	public KeyboardControlTrait_Attack(final int keyChar) {
		super("Attack Control", "Entity attack linked to keyboard inputs.");
		registerGene(coolDown = GeneVector.getExplorationVector().storeGene("player.attack.cooldown",
				new Gene("Cool Down Delay", "The projectile cooldown.", 0, 1000, 64), false));
		registerGene(spread = GeneVector.getExplorationVector().storeGene("player.attack.spread",
				new Gene("Launch Spread", "Bullet spread.", 0, 1, 0), false));
		registerGene(wiggleBigness = GeneVector.getExplorationVector().storeGene("player.attack.wiggle",
				new Gene("Wiggle", "Maximum wiggle magnitude."), false));
		registerGene(amount = GeneVector.getExplorationVector().storeGene("player.attack.amount",
				new Gene("# of Bullets", "Amount of bullets per salvo.", 0, 100, 1), false));
		registerGene(damage = GeneVector.getExplorationVector().storeGene("player.attack.damage",
				new Gene("Damage Per Bullet", "Amount of damage per bullet.", 0, 10, 5), false));
		registerGene(dx = GeneVector.getExplorationVector().storeGene("player.attack.dx",
				new Gene("dx", "dx travel per step", -1, 1, 0), false));
		registerGene(dy = GeneVector.getExplorationVector().storeGene("player.attack.dy",
				new Gene("dy", "dy travel per step", -1, 1, 1), false));
		
		this.key = keyChar;
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		
		if (cd < coolDown.getValue()) {
			cd += delta;
		}
		if (cd >= coolDown.getValue()) {
			if (Keyboard.isKeyDown(key)) {
				cd = 0;
				for (int i = 0; i < amount.getValue(); i++) {
					Entity p = EntityFactory.buildProjectile(self.x, self.y, 4, CollisionLabels.PLAYER_LABEL,
							Color.GREEN, damage.getValue());
					p.addTrait(new DieOffScreenTrait());
					p.addTrait(new TimedDeathTrait(10));
					
					WiggleTrait w = new WiggleTrait(20 * wiggleBigness.getExpression());
					w.wiggleChance.setExpression(1);
					w.wiggleIntensity.setExpression(1);
					
					p.addTrait(w);
					
					TravelSimple t = new TravelSimple();
					
					t.dx.setExpression(dx.getExpression() + (float) Math.random() * (spread.getExpression())
							* (Math.random() < .5 ? -1 : 1) / 2);
					t.dy.setExpression(dy.getExpression());
					
					p.addTrait(t);
					
					RadiusEditTrait rad = new RadiusEditTrait(3, 10, 10);
					p.addTrait(rad);
					
					p.addTrait(new GeneExpressionInterpolator(rad.radius, 0, 1, 200));
					
					level.addEntity(p);
				}
			}
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
