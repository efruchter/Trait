package efruchter.tp.traits;

import java.util.ArrayList;
import java.util.List;

import efruchter.tp.entities.Behavior;
import efruchter.tp.traits.genes.Gene;



public abstract class Trait implements Behavior {

	private String name, info;

	public Trait(String name, String info) {
		this.name = name;
		this.info = info;
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

	protected List<Gene> genes = new ArrayList<Gene>();

	public void registerGene(Gene gene) {
		genes.add(gene);
	}

	public List<Gene> getGenes() {
		return genes;
	}

}
