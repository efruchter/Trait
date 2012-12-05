package efruchter.tp.trait.custom.player;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.gene.Gene;

/**
 * Link the radius of an entity to a gene.
 * 
 * @author toriscope
 * 
 */
public class PlayerRadiusEditTrait extends Trait {
	
	public Gene radius;
	
	public PlayerRadiusEditTrait(float minR, float maxR, float var) {
		super("Variable Radius", "Has a variable radius.");
		registerGene(radius = GeneVector.getExplorationVector().storeGene("player.radius.radius",
				new Gene("Radius", "The radius value", minR, maxR, var), false));
	}
	
	@Override
	public void onStart(Entity self, Level level) {
		
	}
	
	@Override
	public void onUpdate(Entity self, Level level, long delta) {
		self.radius = radius.getValue();
	}
	
	@Override
	public void onDeath(Entity self, Level level) {
		
	}
	
}
