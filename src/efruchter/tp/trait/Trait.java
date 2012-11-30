package efruchter.tp.trait;

import java.util.ArrayList;
import java.util.List;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.gene.Gene;

/**
 * Trait, composed of genes. This should convert its genes into some sort of
 * behavior. Genes should be exposed for editing. A Heavy-duty behavior meant to
 * 
 * @author toriscope
 * 
 */
public abstract class Trait implements Behavior {
	
	protected String name, info;
	protected final List<Gene> genes;
	private boolean active;
	
	public Trait(String name, String info) {
		this.name = name;
		this.info = info;
		genes = new ArrayList<Gene>();
		active = true;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public void registerGene(Gene... genes) {
		for (Gene g : genes)
			this.genes.add(g);
	}
	
	public List<Gene> getGenes() {
		return genes;
	}
	
	@Override
	public String toString() {
		return "(T) " + name + " (" + (isActive() ? "ON" : "OFF") + ")";
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public class TraitAdapter extends Trait {
		
		public TraitAdapter(String name, String info) {
			super(name, info);
		}
		
		@Override
		public void onStart(Entity self, Level level) {
			
		}
		
		@Override
		public void onUpdate(Entity self, Level level, long delta) {
			
		}
		
		@Override
		public void onDeath(Entity self, Level level) {
			
		}
	}
}
