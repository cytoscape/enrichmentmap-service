package ca.utoronto.tdccbr.services.enrichmentmap.task;

import java.util.Arrays;
import java.util.Collection;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;

public class FilterGenesetsByDatasetGenesTask implements Task {

	private final EnrichmentMap map;

	public FilterGenesetsByDatasetGenesTask(EnrichmentMap map) {
		this.map = map;
	}

	@Override
	public void run() throws Exception {
		filterGenesets();
	}
	
	/**
	 * Filter all the genesets by the dataset genes. If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes.
	 */
	private void filterGenesets() {
		var datasets = map.getDataSets();
		
		if (map.getParams().isFilterByExpressions()) {
			for (var k : datasets.keySet()) {
				var currentSet = datasets.get(k);
				
				// Only filter the genesets if dataset genes are not null or empty
				// TODO yeah but we fill the data set with Dummy expressions, so dataSet genes will never by empty right?
				var expressionGenes = currentSet.getExpressionGenes();
				
				if (expressionGenes != null && !expressionGenes.isEmpty())
					currentSet.getGeneSetsOfInterest().filterGeneSets(expressionGenes);
				else
					System.out.println("Dataset Genes is empty, because expression and ranks not provided: " + currentSet.getName());
			}
	
			// check to make sure that after filtering there are still genes in the genesets
			// if there aren't any genes it could mean that the IDs don't match or it could mean none
			// of the genes in the expression file are in the specified genesets.
			if (!anyGenesLeftAfterFiltering(datasets.values()))
				throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");
		}
		
		// if there are multiple datasets check to see if they have the same set of genes
		if (datasetsAreDistinct(map))
			map.setDistinctExpressionSets(true);
		else if (expressionValuesAreCommon(map)) // We only compress the heatmap if the expression values are the same
			map.setCommonExpressionValues(true);
	}

	private static boolean datasetsAreDistinct(EnrichmentMap map) {
		var iter = map.getDataSets().values().iterator();
		var genes = iter.next().getExpressionGenes();
		
		while (iter.hasNext()) {
			if (!genes.equals(iter.next().getExpressionGenes()))
				return true;
		}
		
		return false;
	}
	
	private static boolean expressionValuesAreCommon(EnrichmentMap map) {
		// If there is only one expression matrix then its obviously not distinct.
		if (map.getExpressionMatrixKeys().size() != 1)
			return false;
		
		var iter = map.getDataSets().values().iterator();
		var r = iter.next().getEnrichments();
		var p1 = r.getPhenotype1();
		var p2 = r.getPhenotype2();
		var ps = r.getPhenotypes();
		
		while (iter.hasNext()) {
			var r2 = iter.next().getEnrichments();

			if (!p1.equals(r2.getPhenotype1()))
				return false;
			if (!p2.equals(r2.getPhenotype2()))
				return false;
			if (!Arrays.equals(ps, r2.getPhenotypes()))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Check to see that there are genes in the filtered genesets If the ids do
	 * not match up, after a filtering there will be no genes in any of the genesets
	 * 
	 * @return true if Genesets have genes, return false if all the genesets are empty
	 */
	private static boolean anyGenesLeftAfterFiltering(Collection<EMDataSet> datasets) {
		for (var ds : datasets) {
			var genesets = ds.getGeneSetsOfInterest().getGeneSets();
			
			for (var gs : genesets.values()) {
				var gsGenes = gs.getGenes();
				
				// if there is at least one gene in any of the genesets then the ids match.
				if (!gsGenes.isEmpty())
					return true;
			}
		}
		
		return false;
	}
}
