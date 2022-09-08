package ca.utoronto.tdccbr.services.enrichmentmap.task;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import ca.utoronto.tdccbr.services.enrichmentmap.model.Columns;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentResult;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GSEAResult;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenericResult;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenesetSimilarity;
import ca.utoronto.tdccbr.services.enrichmentmap.model.SimilarityKey;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable.CyRow;

public class CreateEMNetworkTask implements Task {

	private final EnrichmentMap map;
	private final String prefix = Columns.NAMESPACE_PREFIX;

	private final Supplier<Map<SimilarityKey, GenesetSimilarity>> similaritySupplier;
	private final Consumer<CyNetwork> networkConsumer;

	private CyNetwork emNetwork;

	public CreateEMNetworkTask(
		EnrichmentMap map,
		Supplier<Map<SimilarityKey, GenesetSimilarity>> similaritySupplier,
		Consumer<CyNetwork> networkConsumer
	) {
		this.map = map;
		this.similaritySupplier = similaritySupplier;
		this.networkConsumer = networkConsumer;
	}
	
	@Override
	public void run() {
		emNetwork = createEMNetwork();
		if(networkConsumer != null)
			networkConsumer.accept(emNetwork);
	}
	
	
	public CyNetwork getNetwork() {
		return emNetwork;
	}
	
	private CyNetwork createEMNetwork() {
		// Create the CyNetwork
		var network = new CyNetwork();
		
		createNodeColumns(network);
		createEdgeColumns(network);
		
		var nodes = createNodes(network);
		createEdges(network, nodes);
		
		return network;
	}
	
	private Map<String, CyNode> createNodes(CyNetwork network) {
		var nodes = new HashMap<String, CyNode>();
		var geneSets = map.unionAllGeneSetsOfInterest();
		
		for (var gsName : geneSets.keySet()) {
			var node = network.addNode();
			nodes.put(gsName, node);
			
			// Set common attributes
			var row = network.getRow(node);
			Columns.NODE_NAME.set(row, prefix, null, gsName); // MKTODO why is this column needed?
			
			var gs = map.getGeneSet(gsName);
			
			if (gs != null) {
				Columns.NODE_GS_DESCR.set(row, prefix, null, gs.getLabel());
				
//				if (map.getParams().isParseBaderlabGeneSets()) {
//					if (gs.getSource().isPresent())
//						Columns.NODE_DATASOURCE.set(row, prefix, null, gs.getSource().get());
//					if (gs.getDatasourceId().isPresent())
//						Columns.NODE_DATASOURCEID.set(row, prefix, null, gs.getDatasourceId().get());
//				}
				
//				if (map.getParams().isDavid()) {
//					if (gs.getDavidCategory().isPresent())
//						Columns.NODE_DAVID_CATEGORY.set(row, prefix, null, gs.getDavidCategory().get());
//				}
			}
			
			Columns.NODE_GS_TYPE.set(row, prefix, null, Columns.NODE_GS_TYPE_ENRICHMENT);
			var geneIds = geneSets.get(gsName);
			var genes = geneIds.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
			Columns.NODE_GENES.set(row, prefix, null, genes);
			Columns.NODE_GS_SIZE.set(row, prefix, null, genes.size());
			
			// Set attributes specific to each dataset
			for (var ds : map.getDataSetList()) {
				if (ds.getGeneSetsOfInterest().getGeneSets().containsKey(gsName))
					ds.addNodeId(node.getID());
				
				var enrichmentResults = ds.getEnrichments().getEnrichments();
				var result = enrichmentResults.get(gsName);
				
				// if result is null it will fail both instanceof checks
				if (result instanceof GSEAResult)
					setGSEAResultNodeAttributes(row, ds, (GSEAResult) result);
				else if (result instanceof GenericResult)
					setGenericResultNodeAttributes(row, ds, (GenericResult) result);
			}
		}
		
		return nodes;
	}
	
	/**
	 * Note, we expect that GenesetSimilarity object that don't pass the cutoff have already been filtered out.
	 * @param network
	 * @param nodes
	 */
	private void createEdges(CyNetwork network, Map<String,CyNode> nodes) {
		var similarities = similaritySupplier.get();
		
		for (var key : similarities.keySet()) {
			var similarity = similarities.get(key);
			
			var node1 = nodes.get(similarity.getGeneset1Name());
			var node2 = nodes.get(similarity.getGeneset2Name());
			
			var edge = network.addEdge(node1, node2);
			
			var dsName = key.getName();
			
			if (dsName != null) {
				var ds = map.getDataSet(dsName);
				
				if (ds != null)
					ds.addEdgeId(edge.getID());
			}
			
			var overlapGenes = 
				similarity.getOverlappingGenes().stream()
				.map(map::getGeneFromHashKey)
				.collect(Collectors.toList());
			
			var edgeName = key.toString();
			
			var row = network.getRow(edge);
			row.set(CyNetwork.NAME, edgeName);
			row.set(CyEdge.INTERACTION, similarity.getInteractionType());
			Columns.EDGE_SIMILARITY_COEFF.set(row, prefix, null, similarity.getSimilarityCoeffecient());
			Columns.EDGE_OVERLAP_SIZE.set(row, prefix, null, similarity.getSizeOfOverlap());
			Columns.EDGE_OVERLAP_GENES.set(row, prefix, null, overlapGenes);

			if (key.isCompound())
				Columns.EDGE_DATASET.set(row, prefix, null, Columns.EDGE_DATASET_VALUE_COMPOUND);
			else
				Columns.EDGE_DATASET.set(row, prefix, null, similarity.getDataSetName());
		}
	}
	
	private CyTable createNodeColumns(CyNetwork network) {
//		var params = map.getParams();
		var table = network.getNodeTable();
		
		Columns.NODE_NAME.createColumn(table, prefix, null);// !
		Columns.NODE_GS_DESCR.createColumn(table, prefix, null);// !
		Columns.NODE_GS_TYPE.createColumn(table, prefix, null);// !
		Columns.NODE_GENES.createColumn(table, prefix, null); // Union of geneset genes across all datasets // !
		Columns.NODE_GS_SIZE.createColumn(table, prefix, null); // Size of the union // !
		
//		if (params.isParseBaderlabGeneSets()) {
//			Columns.NODE_DATASOURCE.createColumn(table, prefix, null);
//			Columns.NODE_DATASOURCEID.createColumn(table, prefix, null);
//		}
		
//		if (params.isDavid())
//			Columns.NODE_DAVID_CATEGORY.createColumn(table, prefix, null);
		
		for (var ds : map.getDataSetList()) {
			Columns.NODE_PVALUE.createColumn(table, prefix, ds);
			Columns.NODE_FDR_QVALUE.createColumn(table, prefix, ds);
			Columns.NODE_FWER_QVALUE.createColumn(table, prefix, ds);
			// MKTODO only create these if method is GSEA?
			Columns.NODE_ES.createColumn(table, prefix, ds);
			Columns.NODE_NES.createColumn(table, prefix, ds); 
			Columns.NODE_COLOURING.createColumn(table, prefix, ds);
			
//			params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, ds));
//			params.addQValueColumnName(Columns.NODE_FDR_QVALUE.with(prefix, ds));
		}
		
		return table;
	}
	
	private CyTable createEdgeColumns(CyNetwork network) {
		var table = network.getEdgeTable();
		Columns.EDGE_SIMILARITY_COEFF.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_SIZE.createColumn(table, prefix, null);
		Columns.EDGE_DATASET.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_GENES.createColumn(table, prefix, null);
		
//		map.getParams().addSimilarityCutoffColumnName(Columns.EDGE_SIMILARITY_COEFF.with(prefix, null));
		
		return table;
	}
	
	private void setGenericResultNodeAttributes(CyRow row, EMDataSet dataset, GenericResult result) {
		Columns.NODE_PVALUE.set(row, prefix, dataset, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, dataset, result.getFdrqvalue());
		Columns.NODE_NES.set(row, prefix, dataset, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, dataset, getColorScore(result));
	}
	
	private void setGSEAResultNodeAttributes(CyRow row, EMDataSet dataset, GSEAResult result) {
		Columns.NODE_PVALUE.set(row, prefix, dataset, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, dataset, result.getFdrqvalue());
		Columns.NODE_FWER_QVALUE.set(row, prefix, dataset, result.getFwerqvalue());
		Columns.NODE_ES.set(row, prefix, dataset, result.getES());
		Columns.NODE_NES.set(row, prefix, dataset, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, dataset, getColorScore(result));
		
//		var params = map.getParams();
//		params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, dataset));
	}
	
	private static double getColorScore(EnrichmentResult result) {
		if (result == null)
			return 0.0;
		
		double nes;
		
		if (result instanceof GSEAResult)
			nes = ((GSEAResult)result).getNES();
		else
			nes = ((GenericResult)result).getNES();
			
		if (nes >= 0)
			return 1 - result.getPvalue();
		else
			return (-1) * (1 - result.getPvalue());
	}
}
