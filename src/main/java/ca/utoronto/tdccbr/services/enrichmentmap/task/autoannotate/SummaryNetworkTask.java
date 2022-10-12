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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge.Type;
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
		String getLabel();
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
		@Override
		public String getLabel() {
			return cluster.getLabel();
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
		@Override
		public String getLabel() {
//			CyNetwork network = annotationSet.getParent().getNetwork();
//			String label = network.getRow(node).get(annotationSet.getLabelColumn(), String.class);
//			return label;
			// TODO return the name of the gene set
			return "blah";
		}
	}
	
	
	/**
	 * Represents a summary CyNetwork where each node corresponds to a Cluster in the origin network.
	 */
	private class SummaryNetwork {
		final CyNetwork network;
		// One-to-one mapping between Clusters in the origin network and CyNodes in the summary network.
		private final BiMap<SummaryCluster,CyNode> clusterToSummaryNetworkNode;
		
		SummaryNetwork() {
			this.network = new CyNetwork();
			this.clusterToSummaryNetworkNode = HashBiMap.create();
		}
		
		void addNode(SummaryCluster cluster) {
			CyNode node = network.addNode();
			clusterToSummaryNetworkNode.put(cluster, node);
		}
		
		void addEdge(MetaEdge metaEdge) {
			CyNode source = clusterToSummaryNetworkNode.get(metaEdge.source);
			CyNode target = clusterToSummaryNetworkNode.get(metaEdge.target);
			network.addEdge(source, target);
		}
		
		CyNode getNodeFor(SummaryCluster cluster) {
			return clusterToSummaryNetworkNode.get(cluster);
		}
		
		SummaryCluster getClusterFor(CyNode node) {
			return clusterToSummaryNetworkNode.inverse().get(node);
		}
		
		Collection<SummaryCluster> getClusters() {
			return clusterToSummaryNetworkNode.keySet();
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
		
		// create summary network
		SummaryNetwork summaryNetwork = createSummaryNetwork(originNetwork, summaryClusters);
		if(summaryNetwork == null)
			return;
		
//		// create summary network view
//		CyNetworkView summaryNetworkView = createNetworkView(summaryNetwork);
//		if(cancelled)
//			return;
		
		// apply visual style
//		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": apply visual style");
//		applyVisualStyle(annotationSet.getParent().getNetworkView(), summaryNetworkView, summaryNetwork);
//		if(cancelled)
//			return;
		
		// register
//		summaryNetwork.network.getRow(summaryNetwork.network).set(CyNetwork.NAME, "AutoAnnotate - Summary Network");
//		networkManager.addNetwork(summaryNetwork.network);
//		networkViewManager.addNetworkView(summaryNetworkView);
//		summaryNetworkView.fitContent();
		
//		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": done");
		resultNetwork = summaryNetwork.network;
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
		Set<MetaEdge> metaEdges = findMetaEdges(originNetwork, clusters);
		
		SummaryNetwork summaryNetwork = new SummaryNetwork();
		clusters.forEach(summaryNetwork::addNode);
		metaEdges.forEach(summaryNetwork::addEdge);
		
		aggregateAttributes(originNetwork, summaryNetwork);
		return summaryNetwork;
	}
	
	
	private Set<MetaEdge> findMetaEdges(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		Map<CyNode, SummaryCluster> nodeToCluster = new HashMap<>();
		for(SummaryCluster cluster : clusters) {
			for(CyNode node : cluster.getNodes()) {
				nodeToCluster.put(node, cluster);
			}
		}
		
		Set<MetaEdge> metaEdges = new HashSet<>();
		for(SummaryCluster sourceCluster : clusters) {
			Set<CyNode> targets = getAllTargets(originNetwork, sourceCluster);
			for(CyNode target : targets) {
				SummaryCluster targetCluster = nodeToCluster.get(target);
				if(targetCluster != null) {
					metaEdges.add(new MetaEdge(sourceCluster, targetCluster));
				}
			}
		}
		return metaEdges;
	}
	
	/**
	 * Returns all nodes outside the cluster that are connected to a node in the cluster.
	 */
	private Set<CyNode> getAllTargets(CyNetwork network, SummaryCluster c) {
		Set<CyNode> targets = new HashSet<>();
		Collection<CyNode> nodes = c.getNodes();
		for(CyNode node : nodes) {
			for(CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
				CyNode source = edge.getSource();
				CyNode target = edge.getTarget();
				
				if(!nodes.contains(source)) {
					targets.add(source);
				}
				else if(!nodes.contains(target)) {
					targets.add(target);
				}
			}
		}
		return targets;
	}
	
	
	
//	private CyNetworkView createNetworkView(SummaryNetwork summaryNetwork) {
//		CyNetworkView networkView = networkViewFactory.createNetworkView(summaryNetwork.network);
//		for(View<CyNode> nodeView : networkView.getNodeViews()) {
//			SummaryCluster cluster = summaryNetwork.getClusterFor(nodeView.getModel());
//			Point2D.Double center = cluster.getCoordinateData().getCenter();
//			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, center.x);
//			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, center.y);
//		}
//		return networkView;
//	}
	
	
	
	private void aggregateAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originNodeTable = originNetwork.getNodeTable();
		
		CyTable summaryNodeTable = summaryNetwork.network.getNodeTable();
		summaryNodeTable.createColumn("cluster node count", Integer.class);
		
		List<String> columnsToAggregate = new ArrayList<>();
		
		for(CyColumn column : originNodeTable.getColumns()) {
			String name = column.getName();
			if(summaryNodeTable.getColumn(name) == null) {
				columnsToAggregate.add(name);
				Class<?> listElementType = column.getListElementType();
				if(listElementType == null) {
					summaryNodeTable.createColumn(name, column.getType());
				}
				else {
					summaryNodeTable.createListColumn(name, listElementType);
				}
			}
		}
		
		Collection<SummaryCluster> clusters = summaryNetwork.getClusters();
		for(SummaryCluster cluster : clusters) {
			CyNode summaryNode = summaryNetwork.getNodeFor(cluster);
			CyRow row = summaryNodeTable.getRow(summaryNode.getID());
			
			row.set("name", cluster.getLabel());
			row.set("cluster node count", cluster.getNodes().size());
			
			for(String columnName : columnsToAggregate) {
				Object result = aggregate(originNetwork, cluster, columnName);
				row.set(columnName, result);
			}
		}
	}
	
	
	private Object aggregate(CyNetwork originNetwork, SummaryCluster cluster, String columnName) {
		CyTable originNodeTable = originNetwork.getNodeTable();
		CyColumn originColumn = originNodeTable.getColumn(columnName);
		
		Aggregator<?> aggregator = getAggregator(originColumn);
		if(aggregator == null)
			return null;
		
		try {
			ArrayList<CyNode> nodes = new ArrayList<>(cluster.getNodes());
			return aggregator.aggregate(originNodeTable, nodes, originColumn);
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
	
	
//	private static AttributeHandlingType getAttributeHandlingType(Aggregator<?> aggregator) {
//		for(AttributeHandlingType type : AttributeHandlingType.values()) {
//			if(type.toString().equals(aggregator.toString())) {
//				return type;
//			}
//		}
//		return null;
//	}
	
	
	
//	private void applyVisualStyle(CyNetworkView originNetworkView, CyNetworkView summaryNetworkView, SummaryNetwork summaryNetwork) {
//		VisualStyle vs = visualMappingManager.getVisualStyle(originNetworkView);
//		
//		for(View<CyNode> nodeView : summaryNetworkView.getNodeViews()) {
//			// Label
//			String name = summaryNetworkView.getModel().getRow(nodeView.getModel()).get("name", String.class);
//			nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL, name);
//			
//			// Node size
////			CyNode node = nodeView.getModel();
////			SummaryCluster cluster = summaryNetwork.getClusterFor(node);
////			int numNodes = cluster.getNodes().size();
//			nodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, 100.0);
//		}
//		
//		visualMappingManager.setVisualStyle(vs, summaryNetworkView);
//	}


//	@Override
//	public <R> R getResults(Class<? extends R> type) {
//		if(CyNetwork.class.equals(type)) {
//			return type.cast(resultNetwork);
//		}
//		if(String.class.equals(type)) {
//			return type.cast(String.valueOf(resultNetwork.getSUID()));
//		}
//		return null;
//	}
	
	public CyNetwork getSummaryNetwork() {
		return resultNetwork;
	}
	
}

