package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class represents a set of genesets.  In GSEA the set of genesets is contained in a gmt file.
 */
public class SetOfGeneSets {

	/** 
	 * The set of genesets
	 * Hash Key = name of gene set
	 * Hash Value = Gene set
	 * 
	 * Note: Must declare the type as HashMap because it forces GSON to deserialize using 
	 * an actual HashMap, otherwise it wants to use a LinkedTreeMap which uses much more memory.
	 */
	private HashMap<String, GeneSet> geneSets = new HashMap<>();

	/**
	 * FilterGenesets - restrict the genes contained in each gene set to only
	 * the genes found in the expression file.
	 */
	public void filterGeneSets(Set<Integer> expressionGenes) {
		var filteredGenesets = new HashMap<String, GeneSet>();

		// iterate through each geneset and filter each one
		for (var geneSetName : geneSets.keySet()) {
			var gs = geneSets.get(geneSetName);
			var newGs = gs.intersectionWith(expressionGenes);
			filteredGenesets.put(geneSetName, newGs);
		}

		geneSets = filteredGenesets;
	}

	public Collection<String> getGeneSetNames() {
		return Collections.unmodifiableCollection(geneSets.keySet());
	}

	public Map<String, GeneSet> getGeneSets() {
		return geneSets;
	}

	public void setGeneSets(HashMap<String, GeneSet> geneSets) {
		this.geneSets = geneSets;
	}

	public void addGeneSet(String key, GeneSet geneSet) {
		synchronized (geneSet) {
			geneSets.put(key, geneSet);
		}
	}

	public GeneSet getGeneSetByName(String name) {
		if (geneSets != null) {
			if (geneSets.containsKey(name))
				return geneSets.get(name);
		}

		return null;
	}
	
	/**
	 * @return The number of gene sets in this set
	 */
	public int size() {
		return geneSets.size();
	}

	public void clear() {
		geneSets.clear();
	}
	
	public boolean isEmpty() {
		return geneSets.isEmpty();
	}

	@Override
	public int hashCode() {
		return geneSets.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SetOfGeneSets) {
			var other = (SetOfGeneSets) obj;
			return geneSets.equals(other.geneSets);
		}
		
		return false;
	}
}
