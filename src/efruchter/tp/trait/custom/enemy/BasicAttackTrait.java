package efruchter.tp.trait.custom.enemy;

import java.awt.Color;


import efruchter.tp.TraitProjectClient;
import efruchter.tp.entity.CollisionLabel;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.EntityFactory;
import efruchter.tp.entity.EntityType;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.MoveTrait;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.custom.DieOffScreenTrait;
import efruchter.tp.trait.custom.RadiusEditTrait;
import efruchter.tp.trait.custom.TimedDeathTrait;
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
//    private final float bigness;
    public final Gene coolDown, damage, bulletSpeed, bulletSize;
    public boolean tracking;

    public BasicAttackTrait(final boolean tracking, final float bigness, final Gene bulletMoveSpeed, final Gene bulletBigness, final Gene bulletDamage, final Gene bulletCooldown) {
        super("Basic Attack", "An auto-attack.");
//        this.coolDown = new Gene("enemy.bullet.cooldown", "The projectile cooldown.", 0, 1000, 500);
//        this.damage = new Gene("enemy.bullet.damage", "Amount of damage per bullet.", 0, 100, 5);
        this.coolDown = new Gene("enemy.bullet.cooldown", "The projectile cooldown.", bulletCooldown.getMinValue(), bulletCooldown.getMaxValue(), bulletCooldown.getValue());
        this.damage = new Gene("enemy.bullet.damage", "Amount of damage per bullet.", bulletDamage.getMinValue(), bulletDamage.getMaxValue(), bulletDamage.getValue());
        this.bulletSpeed = new Gene("enemy.bullet.speed", "Enemey bullet speed.", bulletMoveSpeed.getMinValue(), bulletMoveSpeed.getMaxValue(), bulletMoveSpeed.getValue());
        this.tracking = tracking;
        this.bulletSize = new Gene("enemy.bullet.size", "Enemy bullet size.", bulletBigness.getMinValue(), bulletBigness.getMaxValue(), bulletBigness.getValue());
    }

    @Override
    public void onStart(final Entity self, final Level level) {
        cd = coolDown.getValue();
    }

    @Override
    public void onUpdate(final Entity self, final Level level, final long delta) {

        if (cd < coolDown.getValue()) {
            cd += delta;
        }
        if (cd >= coolDown.getValue()) {
            cd = 0;
            final Entity p = level.getBlankEntity(EntityType.PROJECTILE);
            EntityFactory.buildProjectile(p, self.x, self.y, bulletSize.getValue(), CollisionLabel.ENEMY_LABEL, Color.ORANGE, damage.getValue());
            p.addTrait(new DieOffScreenTrait());
            p.addTrait(new TimedDeathTrait(10));

            final MoveTrait t;
            if (tracking && level.getPlayer() != null) {
                t = new MoveTrait(level.getPlayer().x - self.x, level.getPlayer().y - self.y, bulletSpeed.getValue(), true);
            } else {
            	// TODO: there seems to be a bug somewhere as this code does get called when spawning enemies
                t = new MoveTrait(0, -1, bulletSpeed.getValue());
            }

            p.addTrait(t);

            // used to vary bullet size over trajectory
            final RadiusEditTrait rad = new RadiusEditTrait(bulletSize.getMinValue(), bulletSize.getValue(), 0); // scale bullet from smallest to current gene setting
            p.addTrait(rad);

            p.addTrait(new GeneExpressionInterpolator(rad.radius, 0, 1, 200));
            
            p.polarity = self.polarity;
            
            p.addTrait(new TraitAdapter(){
            	@Override
				public void onStart(final Entity self, final Level level) {
					TraitProjectClient.s_fired_enemies++;
				}
				@Override
				public void onDeath(final Entity self, final Level level) {
					if (self.health <= 0 && self.isActive()) {
						TraitProjectClient.s_damage_player += damage.getValue();
						TraitProjectClient.s_hit_player += 1;
						TraitProjectClient.display_score -= 25;
					}
				}
			});

        }
    }

    @Override
    public void onDeath(final Entity self, final Level level) {
    }

}
