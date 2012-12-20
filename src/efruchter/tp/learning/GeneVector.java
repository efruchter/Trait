package efruchter.tp.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import efruchter.tp.trait.gene.Gene;

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
    
    public GeneVector(String data) {
        this();
        fromDataString(data);
    }

    /**
     * Store a gene in the registry. Overwrite existing.
     *
     * @param path path to gene in dot form
     * @param g    gene
     * @return gene entry in library
     */
    public Gene storeGene(String path, Gene g) {
        return storeGene(path, g, true);
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

    public void clear() {
        geneMap.clear();
    }

    public String toDataString() {
        StringBuffer s = new StringBuffer();
        for (Entry<String, Gene> entry : geneMap.entrySet()) {
            s.append(SEPARATOR).append(entry.getKey())
                    .append(SEPARATOR).append(entry.getValue().getInfo())
                    .append(SEPARATOR).append(entry.getValue().getMinValue())
                    .append(SEPARATOR).append(entry.getValue().getMaxValue())
                    .append(SEPARATOR).append(entry.getValue().getValue());
        }
        return s.toString().replaceFirst(SEPARATOR, "");
    }

    public void fromDataString(String data) {
        geneMap.clear();
        String[] strings = data.split(SEPARATOR);
        for (int i = 0; i < strings.length; i += 5) {
            float min = Float.parseFloat(strings[i + 2]);
            float max = Float.parseFloat(strings[i + 3]);
            float val = Float.parseFloat(strings[i + 4]);
            geneMap.put(strings[i], new Gene(strings[i], strings[i + 1], min, max, val));
        }
    }

	public Gene getGene(String string) {
		return geneMap.get(string);
	}
}
