package efruchter.tp.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import efruchter.tp.trait.gene.Gene;

/**
 * Data structure for holding a gene vector.
 * 
 * @author toriscope
 * 
 */
public class GeneVector {
	
	/* Temporary, until server stuff is written -------------- */
	private static final GeneVector SERVER_VECTOR = new GeneVector();
	
	public static GeneVector getExplorationVector() {
		return SERVER_VECTOR;
	}
	
	/* ------------------------------------------------------- */
	
	private final HashMap<String, Gene> geneMap;
	
	public GeneVector() {
		geneMap = new HashMap<String, Gene>();
	}
	
	/**
	 * Store a gene in the registry. Overwrite existing.
	 * 
	 * @param path
	 *            path to gene in dot form
	 * @param g
	 *            gene
	 * @return gene entry in library
	 */
	public Gene storeGene(String path, Gene g) {
		return storeGene(path, g, true);
	}
	
	/**
	 * Store a gene in the registry.
	 * 
	 * @param path
	 *            path to gene in dot form
	 * @param g
	 *            gene
	 * @param overwrite
	 *            true to overwrite existing, false to simply retrieve existing
	 *            gene.
	 * @return gene entry in library
	 */
	public Gene storeGene(String path, Gene g, boolean overwrite) {
		
		if (overwrite || !geneMap.containsKey(path)) {
			geneMap.put(path, g);
		}
		
		return geneMap.get(path);
	}
	
	/**
	 * Get a list of genes as GeneWrappers. No order guarantee.
	 * 
	 * @return list of genes in vector
	 */
	public List<GeneWrapper> getGenes() {
		List<GeneWrapper> g = new ArrayList<GeneWrapper>();
		for (Entry<String, Gene> s : geneMap.entrySet()) {
			g.add(new GeneWrapper(s.getValue(), s.getKey()));
		}
		return g;
	}
	
	public static class GeneWrapper {
		public final Gene gene;
		public final String path;
		
		public GeneWrapper(Gene gene, String path) {
			this.gene = gene;
			this.path = path;
		}
		
		public String toString() {
			return path;
		}
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (GeneWrapper g : getGenes()) {
			b.append("\n").append(g.path).append(" | ").append(g.gene.getExpression());
		}
		return b.toString().replaceFirst("\n", "");
	}
	
	public void clear() {
		geneMap.clear();
	}
}
