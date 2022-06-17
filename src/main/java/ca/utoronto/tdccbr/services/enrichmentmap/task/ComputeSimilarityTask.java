package ca.utoronto.tdccbr.services.enrichmentmap.task;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.EMCreationParametersDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.EMCreationParametersDTO.SimilarityMetric;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenesetSimilarity;
import ca.utoronto.tdccbr.services.enrichmentmap.model.SimilarityKey;

public class ComputeSimilarityTask implements Task {

	private final EnrichmentMap map;
	private final Consumer<Map<SimilarityKey, GenesetSimilarity>> consumer;

	public ComputeSimilarityTask(EnrichmentMap map, Consumer<Map<SimilarityKey, GenesetSimilarity>> consumer) {
		this.map = map;
		this.consumer = consumer;
	}
	
	@Override
	public void run() throws InterruptedException {
		int cpus = Runtime.getRuntime().availableProcessors();
		var executor = Executors.newFixedThreadPool(cpus);

		var map = compute(executor);
		executor.shutdown();
		executor.awaitTermination(3, TimeUnit.HOURS);

		done(map);
	}
	
	private Map<SimilarityKey, GenesetSimilarity> compute(ExecutorService executor) {
		boolean distinct = useDistinctEdges();

		var names = map.getAllGeneSetOfInterestNames();
		var unionedGenesets = distinct ? null : map.unionAllGeneSetsOfInterest();

		var edgeType = map.getParams().getEnrichmentEdgeType();
		var similarities = new ConcurrentHashMap<SimilarityKey, GenesetSimilarity>();
		
		var dataSets = map.getDataSetList();
		
		for (var geneset1Name : names) {
			// Compute similarities in batches,
			// creating a Runnable for every similarity pair would create too many objects
			executor.execute(() -> {
				loop:
				for (var geneset2Name : names) {
					if (Thread.interrupted())
						break loop;
					if (geneset1Name.equalsIgnoreCase(geneset2Name))
						continue; // don't compare two identical gene sets
					
					if (distinct) {
						for (var dataset : dataSets) {
							var key = new SimilarityKey(geneset1Name, geneset2Name, edgeType, dataset.getName());

							if (!similarities.containsKey(key)) {
								var genesets = dataset.getGeneSetsOfInterest().getGeneSets();
								var geneset1 = genesets.get(geneset1Name);
								var geneset2 = genesets.get(geneset2Name);

								if (geneset1 != null && geneset2 != null) {
									// returns null if the similarity coefficient doesn't pass the cutoff
									var similarity = computeGenesetSimilarity(
											map.getParams(),
											geneset1Name,
											geneset2Name,
											geneset1.getGenes(),
											geneset2.getGenes(),
											dataset.getName()
									);
									
									if (similarity != null)
										similarities.put(key, similarity);
								}
							}
						}
					} else {
						var key = new SimilarityKey(geneset1Name, geneset2Name, edgeType, null);

						if (!similarities.containsKey(key)) {
							var geneset1 = unionedGenesets.get(geneset1Name);
							var geneset2 = unionedGenesets.get(geneset2Name);

							// returns null if the similarity coefficient doesn't pass the cutoff
							var similarity = computeGenesetSimilarity(
									map.getParams(),
									geneset1Name,
									geneset2Name,
									geneset1,
									geneset2,
									"compound"
							);
							
							if (similarity != null)
								similarities.put(key, similarity);
						}
					}
				}
			});
		}
		
		return similarities;
	}
	
	private void done(Map<SimilarityKey, GenesetSimilarity> similarities) {
		consumer.accept(similarities);
	}
	
	private boolean useDistinctEdges() {
		switch (map.getParams().getEdgeStrategy()) {
			case DISTINCT: 
				return true;
			case COMPOUND: 
				return false;
			default:
			case AUTOMATIC:
				if (map.getDataSetCount() == 1)
					return true; // doesn't really matter but its more consistent with the common 2-dataset case
				if (map.getDataSetCount() == 2)
					return map.isDistinctExpressionSets(); // emulate EM2 behaviour
				// 3 or more datasets use compound edges
				return false; 
		}
	}

	
	private static double computeSimilarityCoeffecient(
			EMCreationParametersDTO params,
			Set<?> intersection,
			Set<?> union,
			Set<?> genes1,
			Set<?> genes2
	) {
		// Note: Do not call intersection.size() or union.size() more than once on a Guava SetView! 
		// It is a potentially slow operation that needs to be recalcuated each time it is called.
		if (params.getSimilarityMetric() == SimilarityMetric.JACCARD) {
			return (double) intersection.size() / (double) union.size();
		} else if (params.getSimilarityMetric() == SimilarityMetric.OVERLAP) {
			return intersection.size() / Math.min((double) genes1.size(), (double) genes2.size());
		} else {
			// It must be combined. Compute a combination of the overlap and jaccard coefecient. We need both the Jaccard and the Overlap.
			double intersectionSize = intersection.size(); // do not call size() more than once on the same SetView
			
			double jaccard = intersectionSize / union.size();
			double overlap = intersectionSize / Math.min((double) genes1.size(), (double) genes2.size());

			double k = params.getCombinedConstant();

			return (k * overlap) + ((1 - k) * jaccard);
		}
	}
	
	private static GenesetSimilarity computeGenesetSimilarity(
			EMCreationParametersDTO params,
			String geneset1Name,
			String geneset2Name,
			Set<Integer> geneset1,
			Set<Integer> geneset2,
			String dataset
	) {
		var intersection = Sets.intersection(geneset1, geneset2);
		var union = Sets.union(geneset1, geneset2);

		double coeffecient = computeSimilarityCoeffecient(params, intersection, union, geneset1, geneset2);
		
		if (!Double.isFinite(coeffecient))
			return null;
		
		if (coeffecient < params.getSimilarityCutoff())
			return null;
			
		var edgeType = params.getEnrichmentEdgeType();
		var similarity = new GenesetSimilarity(geneset1Name, geneset2Name, coeffecient, edgeType, intersection, dataset);
		
		return similarity;
	}
}
