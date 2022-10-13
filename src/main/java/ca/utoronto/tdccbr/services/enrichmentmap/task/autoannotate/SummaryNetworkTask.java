package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge.Type;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable.CyRow;
import ca.utoronto.tdccbr.services.enrichmentmap.task.Task;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.Aggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.BooleanAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.DoubleAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.DoubleListAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.IntegerAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.IntegerListAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.ListAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.LongAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.LongListAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.NoneAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.StringAggregator;
import ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator.StringListAggregator;

public class SummaryNetworkTask implements Task {

	private final boolean includeUnclustered;
	private final Supplier<CyNetwork> networkSupplier;
	private final String clusterIDColumn;
	
	private Collection<Cluster> clusters;
	private CyNetwork resultNetwork;
	
	
	public SummaryNetworkTask(Supplier<CyNetwork> networkSupplier, String clusterIDColumn, boolean includeUnclustered) {
		this.networkSupplier = networkSupplier;
		this.includeUnclustered = includeUnclustered;
		this.clusterIDColumn = clusterIDColumn;
	}
	
	
	/**
	 * An undirected edge.
	 * The trick of this class is in its hashcode() and equals() methods which ignore edge direction.
	 */
	private class MetaEdge {
		final SummaryCluster source;
		final SummaryCluster target;
		
		MetaEdge(SummaryCluster source, SummaryCluster target) {
			this.source = source;
			this.target = target;
		}
		
		@Override
		public int hashCode() {
			return source.hashCode() + target.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			MetaEdge e2 = (MetaEdge) obj;
			// ignore edge direction
			return (source == e2.source && target == e2.target)
			    || (source == e2.target && target == e2.source);
		}
	}
	
	private interface SummaryCluster {
		Collection<CyNode> getNodes();
	}
	
	private static class NormalCluster implements SummaryCluster {
		private final Cluster cluster;
		NormalCluster(Cluster cluster) {
			this.cluster = cluster;
		}
		@Override
		public Collection<CyNode> getNodes() {
			return cluster.getNodes();
		}
	}
	
	private class SingletonCluster implements SummaryCluster {
		private final CyNode node;
		
		SingletonCluster(CyNode node) {
			this.node = node;
		}
		@Override
		public Collection<CyNode> getNodes() {
			return Collections.singleton(node);
		}
	}
	
	
	/**
	 * Represents a summary CyNetwork where each node corresponds to a Cluster in the origin network.
	 */
	private class SummaryNetwork {
		final CyNetwork network;
		// One-to-one mapping between Clusters in the origin network and CyNodes in the summary network.
		private final Map<SummaryCluster,CyNode> clusterToSummaryNetworkNode;
		private final Map<MetaEdge,CyEdge> metaEdgeToSummaryNetworkEdge;
		private final Map<MetaEdge,Set<CyEdge>> metaEdges;
		
		public SummaryNetwork(Collection<SummaryCluster> clusters, Map<MetaEdge,Set<CyEdge>> metaEdges) {
			this.network = new CyNetwork();
			this.clusterToSummaryNetworkNode = new HashMap<>();
			this.metaEdgeToSummaryNetworkEdge = new HashMap<>();
			this.metaEdges = metaEdges;
			
			clusters.forEach(cluster -> {
				CyNode node = network.addNode();
				clusterToSummaryNetworkNode.put(cluster, node);
			});
			
			metaEdges.forEach((metaEdge, originEdges) -> {
				CyNode source = clusterToSummaryNetworkNode.get(metaEdge.source);
				CyNode target = clusterToSummaryNetworkNode.get(metaEdge.target);
				CyEdge edge = network.addEdge(source, target);
				metaEdgeToSummaryNetworkEdge.put(metaEdge, edge);
			});
		}
		
		Collection<SummaryCluster> getClusters() {
			return clusterToSummaryNetworkNode.keySet();
		}
		
		Collection<MetaEdge> getMetaEdges() {
			return metaEdges.keySet();
		}
		
		Set<CyEdge> getOriginEdges(MetaEdge metaEdge) {
			return metaEdges.get(metaEdge);
		}
		
		CyNode getNodeFor(SummaryCluster cluster) {
			return clusterToSummaryNetworkNode.get(cluster);
		}
		
		CyEdge getEdgeFor(MetaEdge metaEdge) {
			return metaEdgeToSummaryNetworkEdge.get(metaEdge);
		}
		
	}
	
	
	
	
	/**
	 * This is fast because no events are fired while the summary network is being built. 
	 * The summary network and view are registered at the end.
	 */
	@Override
	public void run() {
		CyNetwork originNetwork = networkSupplier.get();
		
		this.clusters = initializeClusters(originNetwork, clusterIDColumn);
		
		List<SummaryCluster> summaryClusters = getSummaryClusters(originNetwork);
		if(summaryClusters == null)
			return;
		
		SummaryNetwork summaryNetwork = createSummaryNetwork(originNetwork, summaryClusters);
		if(summaryNetwork == null)
			return;
		
		this.resultNetwork = summaryNetwork.network;
	}
	
	
	
	private static String getClusterID(CyNetwork network, CyNode node, String clusterIdColumn) {
		CyRow row = network.getRow(node);
		var clusterIdList = row.getList(clusterIdColumn, String.class);
		if(clusterIdList != null && !clusterIdList.isEmpty()) {
			return clusterIdList.get(0);
		}
		return null;
	}
	
	
	private static Collection<Cluster> initializeClusters(CyNetwork originNetwork, String clusterIDColumn) {
		Map<String,Set<CyNode>> clusters = new HashMap<>();
		
		for(CyNode node : originNetwork.getNodeList()) {
			var clusterID = getClusterID(originNetwork, node, clusterIDColumn);
			if(clusterID != null) {
				clusters.computeIfAbsent(clusterID, k -> new HashSet<>()).add(node);
			}
		}
		
		List<Cluster> clusterList = new ArrayList<>();
		for(Set<CyNode> set : clusters.values()) {
			clusterList.add(new Cluster(set));
		}
		return clusterList;
	}
	
	
	private List<SummaryCluster> getSummaryClusters(CyNetwork originNetwork) {
		List<SummaryCluster> summaryClusters = new ArrayList<>();
		
		// Add all the clusters regular clusters
		for(Cluster cluster : clusters) {
			summaryClusters.add(new NormalCluster(cluster));
		}
		
		if(includeUnclustered) {
			Set<CyNode> clusteredNodes = clusters.stream().flatMap(c->c.getNodes().stream()).collect(Collectors.toSet());
			Set<CyNode> allNodes = new HashSet<>(originNetwork.getNodeList());
			Set<CyNode> unclusteredNodes = Sets.difference(allNodes, clusteredNodes);
			
			for(CyNode node : unclusteredNodes) {
				summaryClusters.add(new SingletonCluster(node));
			}
		}
		return summaryClusters;
	}
	
	
	private SummaryNetwork createSummaryNetwork(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		var metaEdges = findMetaEdges(originNetwork, clusters);
		
		var summaryNetwork = new SummaryNetwork(clusters, metaEdges);
		
		aggregateNodeAttributes(originNetwork, summaryNetwork);
		aggregateEdgeAttributes(originNetwork, summaryNetwork);
		
		return summaryNetwork;
	}
	
	
	private Map<MetaEdge,Set<CyEdge>> findMetaEdges(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		Map<CyNode, SummaryCluster> nodeToCluster = new HashMap<>();
		for(SummaryCluster cluster : clusters) {
			for(CyNode node : cluster.getNodes()) {
				nodeToCluster.put(node, cluster);
			}
		}
		
		Map<MetaEdge,Set<CyEdge>> metaEdges = new HashMap<>();
		
		for(SummaryCluster sourceCluster : clusters) {
			var targets = getAllTargets(originNetwork, sourceCluster);
			var targetNodes = targets.getLeft();
			var edges = targets.getRight();
			
			for(CyNode target : targetNodes) {
				SummaryCluster targetCluster = nodeToCluster.get(target);
				
				if(targetCluster != null) {
					var metaEdge = new MetaEdge(sourceCluster, targetCluster);
					metaEdges.put(metaEdge, edges);
				}
			}
		}
		
		return metaEdges;
	}
	
	/**
	 * Returns all nodes outside the cluster that are connected to a node in the cluster.
	 */
	private Pair<Set<CyNode>,Set<CyEdge>> getAllTargets(CyNetwork network, SummaryCluster cluster) {
		Set<CyNode> targetNodes = new HashSet<>();
		Set<CyEdge> edges = new HashSet<>();
		
		Collection<CyNode> clusterNodes = cluster.getNodes();
		
		for(CyNode node : clusterNodes) {
			for(CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
				CyNode source = edge.getSource();
				CyNode target = edge.getTarget();
				
				if(!clusterNodes.contains(source)) {
					targetNodes.add(source);
					edges.add(edge);
				} else if(!clusterNodes.contains(target)) {
					targetNodes.add(target);
					edges.add(edge);
				}
			}
		}
		
		return Pair.of(targetNodes, edges);
	}
	
	
	private static List<String> createColumnsInSummaryTable(CyTable originTable, CyTable summaryTable) {
		List<String> columnsToAggregate = new ArrayList<>();
		
		// Create columns in summary network table.
		for(CyColumn column : originTable.getColumns()) {
			String name = column.getName();
			if(summaryTable.getColumn(name) == null) {
				columnsToAggregate.add(name);
				Class<?> listElementType = column.getListElementType();
				if(listElementType == null) {
					summaryTable.createColumn(name, column.getType());
				}
				else {
					summaryTable.createListColumn(name, listElementType);
				}
			}
		}
		return columnsToAggregate;
	}
	
	
	private void aggregateNodeAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originNodeTable  = originNetwork.getNodeTable();
		CyTable summaryNodeTable = summaryNetwork.network.getNodeTable();
		
		List<String> columnsToAggregate = createColumnsInSummaryTable(originNodeTable, summaryNodeTable);
		
		Collection<SummaryCluster> clusters = summaryNetwork.getClusters();
		for(var cluster : clusters) {
			CyNode summaryNode = summaryNetwork.getNodeFor(cluster);
			CyRow row = summaryNodeTable.getRow(summaryNode.getID());
			
			for(String columnName : columnsToAggregate) {
				var originNodes = cluster.getNodes();
				Object result = aggregate(originNodeTable, originNodes, columnName);
				row.set(columnName, result);
			}
		}
	}
	
	
	private void aggregateEdgeAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originEdgeTable  = originNetwork.getEdgeTable();
		CyTable summaryEdgeTable = summaryNetwork.network.getEdgeTable();
		
		List<String> columnsToAggregate = createColumnsInSummaryTable(originEdgeTable, summaryEdgeTable);
		
		Collection<MetaEdge> metaEdges = summaryNetwork.getMetaEdges();
		for(var metaEdge : metaEdges) {
			CyEdge summaryEdge = summaryNetwork.getEdgeFor(metaEdge);
			CyRow row = summaryEdgeTable.getRow(summaryEdge.getID());
			
			for(String columnName : columnsToAggregate) {
				var originEdges = summaryNetwork.getOriginEdges(metaEdge);
				Object result = aggregate(originEdgeTable, originEdges, columnName);
				row.set(columnName, result);
			}
		}
	}
	
	
	private Object aggregate(CyTable originTable, Collection<? extends CyIdentifiable> eles, String columnName) {
		CyColumn originColumn = originTable.getColumn(columnName);
		
		Aggregator<?> aggregator = getAggregator(originColumn);
		if(aggregator == null)
			return null;
		
		try {
			return aggregator.aggregate(originTable, new ArrayList<>(eles), originColumn);
		} catch(Exception e) {
			return null;
		}
	}
	
	
	private Aggregator<?> getAggregator(CyColumn originColumn) {
		// Special handling for EnrichmentMap dataset chart column.
		if("EnrichmentMap::Dataset_Chart".equals(originColumn.getName())) {
			return new IntegerListAggregator(IntegerListAggregator.Operator.MAX);
		}
		
		Class<?> listElementType = originColumn.getListElementType();
		
		// These are general purpose aggregators, copied from the groups bundle in cytoscape.
		if(listElementType == null) {
			Class<?> type = originColumn.getType();
			if(Integer.class.equals(type)) {
				return new IntegerAggregator(IntegerAggregator.Operator.AVG);
			} else if(Long.class.equals(type)) {
				return new LongAggregator(LongAggregator.Operator.AVG);
			} else if(Boolean.class.equals(type)) {
				return new BooleanAggregator(BooleanAggregator.Operator.OR);
			} else if(Double.class.equals(type)) {
				return new DoubleAggregator(DoubleAggregator.Operator.AVG);
			} else if(String.class.equals(type)) {
				return new StringAggregator(StringAggregator.Operator.CSV);
			} else {
				return new NoneAggregator();
			}
		} else {
			Class<?> type = listElementType;
			if(Integer.class.equals(type)) {
				return new IntegerListAggregator(IntegerListAggregator.Operator.AVG);
			} else if(Long.class.equals(type)) {
				return new LongListAggregator(LongListAggregator.Operator.AVG);
			} else if(Double.class.equals(type)) {
				return new DoubleListAggregator(DoubleListAggregator.Operator.AVG);
			} else if(String.class.equals(type)) {
				return new StringListAggregator(StringListAggregator.Operator.CONCAT);
			} else {
				return new ListAggregator(ListAggregator.Operator.CONCAT);
			} 
		}
	}
	
	
	public CyNetwork getSummaryNetwork() {
		return resultNetwork;
	}
	
}

