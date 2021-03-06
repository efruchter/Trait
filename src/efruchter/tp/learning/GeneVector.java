package efruchter.tp.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneCurve;


/**
 * Structure for holding a gene vector, as well as server communication code.
 *
 * @author toriscope
 */
public class GeneVector {

    public static final String SEPARATOR = "#";

    private final HashMap<String, Gene> geneMap;

    public GeneVector() {
        geneMap = new HashMap<String, Gene>();
    }
    
    public GeneVector(final String data) {
        this();
        fromDataString(data);
    }

    

    /**
     * Store a gene in the registry.
     *
     * @param path      path to gene in dot form
     * @param g         gene
     * @param overwrite true to overwrite existing, false to simply retrieve existing
     *                  gene.
     * @return gene entry in library
     */
    public Gene storeGene(final String path, final Gene g, final boolean overwrite) {

        if (overwrite || !geneMap.containsKey(path)) {
            geneMap.put(path, g);
        }

        return geneMap.get(path);
    }
    
    public GeneCurve storeGeneCurve(final String path, final GeneCurve g, final boolean overwrite) {
    	for (int i = 0; i < g.genes.length; i++) {
    		g.genes[i] = storeGene(path + ".c" + i, g.genes[i], overwrite);
    	}
    	
    	return g;
    }

    /**
     * Get a list of genes as GeneWrappers. No order guarantee.
     *
     * @return list of genes in vector
     */
    public List<GeneWrapper> getGenes() {
        final List<GeneWrapper> g = new ArrayList<GeneWrapper>();
        for (Entry<String, Gene> s : geneMap.entrySet()) {
            g.add(new GeneWrapper(s.getValue(), s.getKey()));
        }
        return g;
    }

    public static class GeneWrapper implements Comparable<GeneWrapper>{
        public final Gene gene;
        public final String path;

        public GeneWrapper(final Gene gene, final String path) {
            this.gene = gene;
            this.path = path;
        }

        public String toString() {
            return path;
        }

        @Override
        public int compareTo(GeneWrapper arg0) {
            return this.path.compareTo(arg0.path);
        }
    }

    public void clear() {
        geneMap.clear();
    }

    public String toDataString() {
    	if (geneMap.isEmpty()) {
    		return "";
    	}
        final StringBuffer s = new StringBuffer();
        for (Entry<String, Gene> entry : geneMap.entrySet()) {
            s.append(SEPARATOR).append(entry.getKey())
                    .append(SEPARATOR).append(entry.getValue().getInfo())
                    .append(SEPARATOR).append(entry.getValue().getMinValue())
                    .append(SEPARATOR).append(entry.getValue().getMaxValue())
                    .append(SEPARATOR).append(entry.getValue().getValue());
        }
        return s.toString().replaceFirst(SEPARATOR, "");
    }

    public void fromDataString(final String data) {
        geneMap.clear();
        if (data == null || data.isEmpty()) {
        	return;
        }
        final String[] strings = data.split(SEPARATOR);
        for (int i = 0; i < strings.length; i += 5) {
            float min = Float.parseFloat(strings[i + 2]);
            float max = Float.parseFloat(strings[i + 3]);
            float val = Float.parseFloat(strings[i + 4]);
            geneMap.put(strings[i], new Gene(strings[i], strings[i + 1], min, max, val));
        }
    }

	public Gene getGene(final String string) {
		return geneMap.get(string);
	}
	
	public GeneWrapper getGeneWrapper(final String string) {
        return new GeneWrapper(geneMap.get(string), string);
    }
}
