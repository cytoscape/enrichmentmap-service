package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentResultFilterParams;

public class EMCreationParametersDTO implements EnrichmentResultFilterParams {

	public static enum SimilarityMetric {
		JACCARD, OVERLAP, COMBINED
	}
	
	public static enum EdgeStrategy {
		AUTOMATIC, DISTINCT, COMPOUND
	}
	
	/*
	 	These are the slider defaults in the EM creation dialog.
	 	sparse
			similarityMetric=JACCARD, similarityCutoff=0.35,
			similarityMetric=JACCARD, similarityCutoff=0.25,
			similarityMetric=COMBINED, similarityCutoff=0.375, combinedConstant = 0.5,
			similarityMetric=OVERLAP, similarityCutoff=0.5,
			similarityMetric=OVERLAP, similarityCutoff=0.25,
		dense
	 */
	
	// Node filtering (gene-sets)
	private double pvalue = 1.0;
	private double qvalue = 0.05;
	private boolean filterByExpressions = false;
	
	// Edge filtering (similarity)
	private SimilarityMetric similarityMetric = SimilarityMetric.JACCARD;
	private double similarityCutoff = 0.25;
	private double combinedConstant = 0.5; // only used with COMBINED
	
	private boolean fdr = true; // If true then q-value is used for filtering.
	
	private EdgeStrategy edgeStrategy = EdgeStrategy.AUTOMATIC;
	private String enrichmentEdgeType = "Geneset_Overlap";
	
	public EMCreationParametersDTO() {
		// Just use default values...
	}
	
	@Override
	public double getPvalue() {
		return pvalue;
	}
	
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}

	@Override
	public double getQvalue() {
		return qvalue;
	}
	
	public void setQvalue(double qvalue) {
		this.qvalue = qvalue;
	}

	public boolean isFilterByExpressions() {
		return filterByExpressions;
	}
	
	public void setFilterByExpressions(boolean filterByExpressions) {
		this.filterByExpressions = filterByExpressions;
	}
	
	public double getSimilarityCutoff() {
		return similarityCutoff;
	}
	
	public void setSimilarityCutoff(double similarityCutoff) {
		this.similarityCutoff = similarityCutoff;
	}

	public double getCombinedConstant() {
		return combinedConstant;
	}

	public void setCombinedConstant(double combinedConstant) {
		this.combinedConstant = combinedConstant;
	}
	
	public SimilarityMetric getSimilarityMetric() {
		return similarityMetric;
	}

	public void setSimilarityMetric(SimilarityMetric similarityMetric) {
		this.similarityMetric = similarityMetric;
	}
	
	@Override
	public boolean isFDR() {
		return fdr;
	}

	public void setFDR(boolean fdr) {
		this.fdr = fdr;
	}
	
	public EdgeStrategy getEdgeStrategy() {
		return edgeStrategy;
	}
	
	public void setEdgeStrategy(EdgeStrategy edgeStrategy) {
		this.edgeStrategy = edgeStrategy;
	}

	public String getEnrichmentEdgeType() {
		return enrichmentEdgeType;
	}

	public void setEnrichmentEdgeType(String enrichmentEdgeType) {
		this.enrichmentEdgeType = enrichmentEdgeType;
	}

	@Override
	public String toString() {
		return "EMCreationParametersDTO [pvalue=" + pvalue + ", qvalue=" + qvalue + ", filterByExpressions="
				+ filterByExpressions + ", similarityMetric=" + similarityMetric + ", similarityCutoff="
				+ similarityCutoff + ", combinedConstant=" + combinedConstant + ", fdr=" + fdr + ", edgeStrategy="
				+ edgeStrategy + ", enrichmentEdgeType=" + enrichmentEdgeType + "]";
	}
}
