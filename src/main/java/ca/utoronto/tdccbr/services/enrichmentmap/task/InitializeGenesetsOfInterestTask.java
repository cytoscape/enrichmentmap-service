package ca.utoronto.tdccbr.services.enrichmentmap.task;

import java.util.LinkedHashSet;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;
import ca.utoronto.tdccbr.services.enrichmentmap.model.SetOfGeneSets;

/**
 * Task to create a subset of the geneset in the total gmt file that contains
 * only the genesets with pvalue and q-value less than threshold values
 * specified by the user.
 */

public class InitializeGenesetsOfInterestTask implements Task {

	private final EnrichmentMap map;

	public enum MissingGenesetStrategy {
		IGNORE,
		FAIL_IMMEDIATELY,
		FAIL_AT_END;
	}
	
	private final MissingGenesetStrategy missingGenesetStrategy;

	public InitializeGenesetsOfInterestTask(EnrichmentMap map, MissingGenesetStrategy strategy) {
		this.map = map;
		this.missingGenesetStrategy = strategy;
	}

	public InitializeGenesetsOfInterestTask(EnrichmentMap map) {
		this(map, MissingGenesetStrategy.FAIL_IMMEDIATELY);
	}

	@Override
	public void run() throws Exception {
		initializeSets();
	}
	
	/**
	 * Filter the genesets, restricting them to only those passing the user
	 * specified thresholds.
	 * 
	 * @return true if successful and false otherwise.
	 */
	public void initializeSets() {
		// Create subset of genesets that contains only the genesets of interest with
		// pvalue and qbalue less than values specified by the user.
		// Go through each Dataset populating the Gene set of interest in each dataset object
		var datasets = map.getDataSets();
		
		// Count how many experiments (DataSets) contain the geneset
//		var minExperiments = map.getParams().getMinExperiments();
//		var occurrences = minExperiments.isPresent() ? new HashMap<String, Integer>() : null;
		var missingGeneSets = new LinkedHashSet<String>();
		
		for (var dsName : datasets.keySet()) {
			var ds = datasets.get(dsName);

			// All these maps use the geneset name as key
			var enrichmentResults = ds.getEnrichments().getEnrichments();
			var genesets = ds.getSetOfGeneSets().getGeneSets();
			var genesetsOfInterest = ds.getGeneSetsOfInterest().getGeneSets();

			// If there are no genesets associated with this dataset then get the complete
			// set assumption being that the gmt file applies to all datasets.
			if (genesets == null || genesets.isEmpty())
				genesets = map.getAllGeneSets();

			// If there are no enrichment Results then do nothing
			if (enrichmentResults == null || enrichmentResults.isEmpty())
				continue;
			
			// Iterate through the GSEA Results to figure out which genesets we want to use
			for (String gsName : enrichmentResults.keySet()) {
				var result = enrichmentResults.get(gsName);

				// Update rank at max for leading edge calculation
//				if (ds.getMethod() == Method.GSEA) {
//					var ranks = ds.getRanksByName(dsName);
//					updateRankAtMax((GSEAResult) result, ranks);
//				}

				if (result.isGeneSetOfInterest(map.getParams())) {
					var gs = genesets.get(gsName);

					if (gs == null) {
						switch (missingGenesetStrategy) {
							case FAIL_IMMEDIATELY:
								throw new MissingGenesetsException(gsName);
							case FAIL_AT_END:
								missingGeneSets.add(gsName);
								break;
							case IGNORE:
								break;
						}
					} else {
						// While we are checking, update the size of the genesets based on post filtered data
						result.setGsSize(gs.getGenes().size());
						
//						if (occurrences != null)
//							occurrences.merge(gsName, 1, (v, d) -> v + 1);
						
						genesetsOfInterest.put(gsName, gs);
					}
				}
			}
		}
		
		if (!missingGeneSets.isEmpty())
			throw new MissingGenesetsException(missingGeneSets);
		
		// Remove gene-sets that don't pass the minimum occurrence cutoff
//		if (occurrences != null) {
//			for (var ds : datasets.values()) {
//				var genesetsOfInterest = ds.getGeneSetsOfInterest().getGeneSets();
//
//				genesetsOfInterest.keySet().removeIf(gs -> occurrences.getOrDefault(gs, 0) < minExperiments.get());
//			}
//		}

		boolean empty = datasets.values().stream()
				.map(EMDataSet::getGeneSetsOfInterest)
				.allMatch(SetOfGeneSets::isEmpty);
		
		if (empty)
			throw new IllegalArgumentException(
					"None of the gene sets have passed the filter. Try relaxing the gene set filter parameters.");
		
		// TODO clear all the genesets that are not "of interest" just to free up memory
	}

//	private void updateRankAtMax(GSEAResult currentResult, Ranking ranks) {
//		// Update the current geneset to reflect score at max
//		if (ranks != null) {
//			var allranks = ranks.getAllRanks();
//			Integer largestRank = Collections.max(allranks);
//
//			// get the max at rank for this geneset
//			int currentRankAtMax = currentResult.getRankAtMax();
//
//			if (currentRankAtMax != -1) {
//				// check the ES score.  If it is negative we need to adjust the rank to count from the end of the list
//				double NES = currentResult.getNES();
//				int genekey = -1;
//				
//				// what gene corresponds to that rank
//				if (NES < 0) {
//					// it is possible that some of the proteins in the rank list won't be rank 2gene
//					// conversion because some of the genes might not be in the genesets
//					// so the size of the list can't be used to trace up from the bottom of the
//					// ranks.  Instead we need to get the max rank used.
//					currentRankAtMax = largestRank - currentRankAtMax;
//
//					//reset the rank at max to reflect that it is counted from the bottom of the list.
//					currentResult.setRankAtMax(currentRankAtMax);
//				}
//				
//				//check to see if this rank is in the conversion map
//				if (ranks.containsRank(currentRankAtMax)) {
//					genekey = ranks.getGene(currentRankAtMax);
//				} else {
//					//if is possible that the gene associated with the max is not found in
//					//our gene 2 rank conversions because the rank by GSEA are off by 1 or two
//					//indexes (maybe a bug on their side).
//					//so depending on the NES score we need to fiddle with the rank to find the
//					//next protein that is the actual gene they are referring to
//
//					while (genekey == -1 && (currentRankAtMax <= largestRank && currentRankAtMax > 0)) {
//						if (NES < 0)
//							currentRankAtMax = currentRankAtMax + 1;
//						else
//							currentRankAtMax = currentRankAtMax - 1;
//						if (ranks.containsRank(currentRankAtMax))
//							genekey = ranks.getGene(currentRankAtMax);
//					}
//				}
//
//				if (genekey > -1) {
//					// what is the score for that gene
//					double scoreAtMax = ranks.getRank(genekey).getScore();
//					currentResult.setScoreAtMax(scoreAtMax);
//					// update the score At max in the EnrichmentResults as well
//				}
//			}
//		}
//	}
}
