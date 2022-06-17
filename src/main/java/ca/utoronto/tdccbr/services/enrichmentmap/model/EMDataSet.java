package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.FGSEAEnrichmentResultDTO;

public class EMDataSet extends AbstractDataSet {
	
	public static enum Method {
		FGSEA, GSEA, Generic, Specialized;
		
		public String getLabel() {
			switch (this) {
				case FGSEA: default: return "FGSEA";
				case GSEA:           return "GSEA";
				case Generic:        return "Generic/gProfiler/Enrichr";
				case Specialized:    return "DAVID/BINGO/Great";
			}
		}
	}
	
	private Method method;
	private List<FGSEAEnrichmentResultDTO> fgseaRes;
	private SetOfEnrichmentResults enrichments = new SetOfEnrichmentResults();
	
	private String expressionKey;
	private String geneSetsKey;
	private Map<String, Ranking> ranks = new HashMap<>();
	private boolean isTwoPhenotypeGeneric;
	
	protected EMDataSet(EnrichmentMap map, String name, List<FGSEAEnrichmentResultDTO> fgseaRes) {
		super(map, name);
		this.method = Method.FGSEA;
		this.fgseaRes = fgseaRes;
	}

	/**
	 * Return all the genes in the expressions.
	 */
	public Set<Integer> getExpressionGenes() {
		return Collections.unmodifiableSet(getExpressionSets().getGeneIds());
	}
	
	/**
	 * Return all the genes in the original (un-filtered) GMT.
	 */
	public Set<Integer> getGeneSetGenes() {
		var genes = new HashSet<Integer>();
		
		for (var geneSet : getSetOfGeneSets().getGeneSets().values()) {
			genes.addAll(geneSet.getGenes());
		}
		
		return genes;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public List<FGSEAEnrichmentResultDTO> getFGSEARes() {
		return fgseaRes;
	}

	public SetOfEnrichmentResults getEnrichments() {
		return enrichments;
	}

	public void setExpressionKey(String key) {
		this.expressionKey = key;
	}

	public String getExpressionKey() {
		return expressionKey;
	}

	public void setGeneSetsKey(String key) {
		this.geneSetsKey = key;
	}

	public String getGeneSetsKey() {
		return geneSetsKey;
	}

	public void setEnrichments(SetOfEnrichmentResults enrichments) {
		this.enrichments = enrichments;
	}

	public synchronized GeneExpressionMatrix getExpressionSets() {
		var map = getMap();
		var matrix = map.getExpressionMatrix(expressionKey);
		
		if (matrix == null) {
			// Avoid NPEs
			matrix = new GeneExpressionMatrix();
			var key = "Lazy_" + UUID.randomUUID().toString();
			setExpressionKey(key);
			map.putExpressionMatrix(key, matrix);
		}
		
		return matrix;
	}

	public synchronized SetOfGeneSets getSetOfGeneSets() {
		var map = getMap();
		var geneSets = map.getGeneSets(geneSetsKey);
		
		if (geneSets == null) {
			// Avoid NPEs
			geneSets = new SetOfGeneSets();
			var key = "Lazy_" + UUID.randomUUID().toString();
			setGeneSetsKey(key);
			map.putGeneSets(key, geneSets);
		}
		
		return geneSets;
	}

	public Map<String, Ranking> getRanks() {
		return ranks;
	}

	public void setRanks(Map<String, Ranking> ranks) {
		this.ranks = ranks;
	}

	public void addRanks(String name, Ranking rank) {
		if (ranks != null && name != null && rank != null)
			ranks.put(name, rank);
	}

	public Ranking getRanksByName(String name) {
		if (ranks != null)
			return ranks.get(name);
		else
			return null;
	}

	public Set<String> getAllRanksNames() {
		var allnames = new HashSet<String>();
		
		if (ranks != null && !ranks.isEmpty()) {
			for (var i = ranks.keySet().iterator(); i.hasNext();) {
				var name = i.next();
				
				if (name != null)
					allnames.add(name);
			}
		}

		return allnames;
	}

	/**
	 * @return true if we have at least one list of gene ranks
	 */
	public boolean haveRanks() {
		return ranks != null && ranks.size() > 0;
	}

	public void createNewRanking(String name) {
		var r = new Ranking();
		ranks.put(name, r);
	}

	public void setIsTwoPhenotypeGeneric(boolean b) {
		isTwoPhenotypeGeneric = b;
	}
	
	public boolean getIsTwoPheotypeGeneric() {
		return method == Method.Generic && isTwoPhenotypeGeneric;
	}
	
	@Override
	public String toString() {
		return "EMDataSet [name=" + getName() + ", method=" + method + "]";
	}
}
