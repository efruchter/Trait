package efruchter.tp.traits.custom.player;

import org.lwjgl.input.Keyboard;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;
import efruchter.tp.entities.Projectile;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;
import efruchter.tp.traits.custom.CollideDamageTrait;
import efruchter.tp.traits.custom.TimedDeathTrait;
import efruchter.tp.traits.custom.TravelSimple;
import efruchter.tp.traits.custom.WiggleTrait;

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
		registerGene(coolDown = new Gene("Delay", "The projectile cooldown.", 0, 64, 1000));
		registerGene(spread = new Gene("Spread", "Bullet spread."));
		registerGene(wiggleBigness = new Gene("Wiggle", "Maximum wiggle magnitude."));
		registerGene(amount = new Gene("Amount", "Amount of bullets per salvo.", 0, 100, 1));
		registerGene(damage = new Gene("Damage", "Amount of damage per bullet.", 0, 10, 1));
		
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
					Projectile p = new Projectile(self.x, self.y, 3);
					p.addTrait(new TimedDeathTrait(1), level);
					
					WiggleTrait w = new WiggleTrait(20 * wiggleBigness.getExpression());
					w.wiggleChance.setExpression(1);
					w.wiggleIntensity.setExpression(1);
					
					p.addTrait(w, level);
					
					TravelSimple t = new TravelSimple();
					
					t.dx.setExpression(.5f + (float) Math.random() * (spread.getExpression())
							* (Math.random() < .5 ? -1 : 1) / 2);
					t.dy.setExpression(1);
					
					p.addTrait(t, level);
					p.collisionLabel = CollisionLabels.PLAYER_LABEL;
					p.addTrait(new CollideDamageTrait(damage.getValue()), level);
					
					level.addEntity(p);
				}
			}
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
