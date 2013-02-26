package efruchter.tp.trait.custom.player;

import efruchter.tp.ClientDefaults;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;

/**
 * Link the radius of an entity to a gene.
 * 
 * @author toriscope
 * 
 */
public class PlayerRadiusEditTrait extends Trait {
	
	public final Gene radius;
	
	public PlayerRadiusEditTrait(final float minR, final float maxR, final float var) {
		super("Variable Radius", "Has a variable radius.");
		
		radius = ClientDefaults.server().getExplorationVector().storeGene("player.radius.radius",
				new Gene("Radius", "Player ship radius", minR, maxR, var), false);
	}
	
	@Override
	public void onStart(final Entity self, final Level level) {
		
	}
	
	@Override
	public void onUpdate(final Entity self, final Level level, final long delta) {
		self.radius = radius.getValue();
	}
	
	@Override
	public void onDeath(final Entity self, final Level level) {
		
	}
	
}
