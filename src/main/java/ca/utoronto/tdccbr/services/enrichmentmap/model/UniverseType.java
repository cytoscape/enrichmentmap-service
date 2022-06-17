package ca.utoronto.tdccbr.services.enrichmentmap.model;

/**
 * The "gene universe", typically used as a parameter to 
 * the Hypergeometric test when running post-analysis.
 */
public enum UniverseType {
	
	GMT, 
	EXPRESSION_SET, 
	INTERSECTION, 
	USER_DEFINED;
	
	public int getGeneUniverse(
			EnrichmentMap map,
			String datasetName,
			int userDefinedUniverseSize
	) {
		var ds = map.getDataSet(datasetName);
		
		switch (this) {
			default:
			case GMT:
				return ds.getGeneSetGenes().size(); // number of unfiltered genes from the original GMT file (GMT)
			case EXPRESSION_SET:
				return ds.getExpressionSets().getExpressionUniverse();
			case INTERSECTION:
				return ds.getExpressionSets().getExpressionMatrix().size();
			case USER_DEFINED:
				return userDefinedUniverseSize;
		}
	}
	
	public int getGeneUniverse(EnrichmentMap map, String dataset) {
		if (this == UniverseType.USER_DEFINED)
			throw new IllegalArgumentException();
		return getGeneUniverse(map, dataset, 0);
	}
}