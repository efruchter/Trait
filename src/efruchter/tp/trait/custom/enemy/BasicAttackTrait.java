package efruchter.tp.trait.custom.enemy;

import java.awt.Color;

import efruchter.tp.defaults.CollisionLabels;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.custom.DieOffScreenTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TimedDeathTrait;
import efruchter.tp.trait.custom.TravelSimple;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneExpressionInterpolator;

/**
 * A vicious, downward enemy attack.
 * 
 * @author toriscope
 * 
 */
public class BasicAttackTrait extends Trait {
	
	private float cd;
	public Gene coolDown, spread, amount, damage;
	public TravelSimple movePlasmid;
	
	/**
	 * Create standard attack controller.
	 * 
	 * @param keyChar
	 *            character to launch salvo.
	 */
	public BasicAttackTrait() {
		super("Basic Attack", "An auto-attack.");
		registerGene(coolDown = new Gene("Cool Down Delay", "The projectile cooldown.", 0, 1000, 0));
		registerGene(spread = new Gene("Launch Spread", "Bullet spread.", 0, 1, 1));
		registerGene(amount = new Gene("# of Bullets", "Amount of bullets per salvo.", 0, 100, .8f));
		registerGene(damage = new Gene("Damage Per Bullet", "Amount of damage per bullet.", 0, 10, 1));
		movePlasmid = new TravelSimple(.5f, .3f);
		registerGene(movePlasmid.dx, movePlasmid.dy);
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		cd = coolDown.getValue();
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		
		if (cd < coolDown.getValue()) {
			cd += delta;
		}
		if (cd >= coolDown.getValue()) {
			cd = 0;
			for (int i = 0; i < amount.getValue(); i++) {
				Entity p = EntityFactory.buildProjectile(self.x, self.y, 4, CollisionLabels.ENEMY_LABEL, Color.ORANGE,
						damage.getValue());
				p.addTrait(new DieOffScreenTrait());
				p.addTrait(new TimedDeathTrait(10));
				
				TravelSimple t = new TravelSimple();
				
				t.dx.setExpression(movePlasmid.dx.getExpression() + (float) Math.random() * (spread.getExpression())
						* (Math.random() < .5 ? -1 : 1) / 2);
				t.dy.setExpression(movePlasmid.dy.getExpression());
				
				p.addTrait(t);
				
				RadiusEditTrait rad = new RadiusEditTrait(3, 10, 10);
				p.addTrait(rad);
				
				p.addTrait(new GeneExpressionInterpolator(rad.radius, 0, 1, 200));
				
				level.addEntity(p);
				
			}
		}
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
