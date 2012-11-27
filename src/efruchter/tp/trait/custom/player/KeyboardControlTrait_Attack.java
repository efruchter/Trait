package efruchter.tp.trait.custom.player;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
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
	public Gene coolDown, spread, wiggleBigness, amount, damage;
	private int key;
	
	/**
	 * Create standard attack controller.
	 * 
	 * @param keyChar
	 *            character to launch salvo.
	 */
	public KeyboardControlTrait_Attack(final int keyChar) {
		super("Attack Control", "Entity attack linked to keyboard inputs.");
		registerGene(coolDown = new Gene("Cool Down Delay", "The projectile cooldown.", 0, 1000, 64));
		registerGene(spread = new Gene("Launch Spread", "Bullet spread."));
		registerGene(wiggleBigness = new Gene("Wiggle", "Maximum wiggle magnitude."));
		registerGene(amount = new Gene("# of Bullets", "Amount of bullets per salvo.", 0, 100, 1));
		registerGene(damage = new Gene("Damage Per Bullet", "Amount of damage per bullet.", 0, 10, 5));
		
		spread.setExpression(0);
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
					p.addTrait(new TimedDeathTrait(1));
					
					WiggleTrait w = new WiggleTrait(20 * wiggleBigness.getExpression());
					w.wiggleChance.setExpression(1);
					w.wiggleIntensity.setExpression(1);
					
					p.addTrait(w);
					
					TravelSimple t = new TravelSimple();
					
					t.dx.setExpression(.5f + (float) Math.random() * (spread.getExpression())
							* (Math.random() < .5 ? -1 : 1) / 2);
					t.dy.setExpression(1);
					
					p.addTrait(t);
					p.collisionLabel = CollisionLabels.PLAYER_LABEL;
					
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
