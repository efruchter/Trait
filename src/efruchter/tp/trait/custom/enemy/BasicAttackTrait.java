package efruchter.tp.trait.custom.enemy;

import java.awt.Color;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.defaults.ClientDefaults;
import efruchter.tp.defaults.CollisionLabel;
import efruchter.tp.defaults.EntityFactory;
import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
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
    private final float bigness;
    public final Gene coolDown, damage;
    public boolean tracking;

    public BasicAttackTrait(final boolean tracking, final float bigness) {
        super("Basic Attack", "An auto-attack.");
        coolDown = new Gene("Cool Down Delay", "The projectile cooldown.", 0, 1000, 500);
        damage = new Gene("Damage Per Bullet", "Amount of damage per bullet.", 0, 10, 1);
        this.tracking = tracking;
        this.bigness = bigness;
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
            EntityFactory.buildProjectile(p, self.x, self.y, 2, CollisionLabel.ENEMY_LABEL, Color.ORANGE, damage.getValue());
            p.addTrait(new DieOffScreenTrait());
            p.addTrait(new TimedDeathTrait(10));

            final MoveTrait t;
            if (tracking && level.getPlayer() != null) {
                t = new MoveTrait(level.getPlayer().x - self.x, level.getPlayer().y - self.y, .25f, true);
            } else {
                t = new MoveTrait(0, -1, .25f);
            }

            p.addTrait(t);

            final RadiusEditTrait rad = new RadiusEditTrait(1, Math.max(8, bigness), 0);
            p.addTrait(rad);

            p.addTrait(new GeneExpressionInterpolator(rad.radius, 0, 1, 200));

            p.addTrait(new TraitAdapter() {
                @Override
                public void onUpdate(final Entity self, final Level level, final long delta) {
                    if (self.health < 0 && level.getPlayer() != null && level.getPlayer().isColliding(self)) {
                        TraitProjectClient.addScore(ClientDefaults.SCORE1_PLAYER_HIT);
                    }
                }
            });
            
            p.polarity = self.polarity;

        }
    }

    @Override
    public void onDeath(final Entity self, final Level level) {
    }

}
