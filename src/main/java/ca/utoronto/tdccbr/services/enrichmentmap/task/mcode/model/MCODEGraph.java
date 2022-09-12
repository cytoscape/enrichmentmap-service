package ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;

public class MCODEGraph {

	private final CyNetwork parentNetwork;
	private final Map<UUID, CyNode> nodeMap;
	private final Map<UUID, CyEdge> edgeMap;
	private CyNetwork subNetwork;
	private boolean disposed;
	
	private final Object lock = new Object();

	public MCODEGraph(CyNetwork parentNetwork, Collection<CyNode> nodes, Collection<CyEdge> edges) {
		if (parentNetwork == null)
			throw new NullPointerException("parentNetwork is null!");
		if (nodes == null)
			throw new NullPointerException("nodes is null!");
		if (edges == null)
			throw new NullPointerException("edges is null!");

		this.parentNetwork = parentNetwork;
		this.nodeMap = new HashMap<>(nodes.size());
		this.edgeMap = new HashMap<>(edges.size());

		for (CyNode n : nodes)
			addNode(n);
		for (CyEdge e : edges)
			addEdge(e);
	}

	public boolean addNode(CyNode node) {
		if (disposed)
			throw new IllegalStateException("This cluster has been disposed.");
		
		nodeMap.put(node.getID(), node);
			
		return true;
	}

	public boolean addEdge(CyEdge edge) {
		if (disposed)
			throw new IllegalStateException("This cluster has been disposed.");
		
		if (containsNode(edge.getSource()) && containsNode(edge.getTarget())) {
			edgeMap.put(edge.getID(), edge);
			
			return true;
		}

		return false;
	}

	public int getNodeCount() {
		return nodeMap.size();
	}

	public int getEdgeCount() {
		return edgeMap.size();
	}

	public Collection<CyNode> getNodeList() {
		return nodeMap.values();
	}

	public Collection<CyEdge> getEdgeList() {
		return edgeMap.values();
	}

	public boolean containsNode(CyNode node) {
		return nodeMap.containsKey(node.getID());
	}

	public boolean containsEdge(CyEdge edge) {
		return edgeMap.containsKey(edge.getID());
	}

	public CyNode getNode(UUID suid) {
		return nodeMap.get(suid);
	}

	public CyEdge getEdge(UUID suid) {
		return edgeMap.get(suid);
	}

	public List<CyEdge> getAdjacentEdgeList(CyNode node, CyEdge.Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getAdjacentEdgeList(node, edgeType);
		List<CyEdge> list = new ArrayList<>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public List<CyEdge> getConnectingEdgeList(CyNode source, CyNode target, CyEdge.Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getConnectingEdgeList(source, target, edgeType);
		List<CyEdge> list = new ArrayList<>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public CyNetwork getParentNetwork() {
		return parentNetwork;
	}

	public boolean isDisposed() {
		synchronized (lock) {
			return disposed;
		}
	}
	
	public void dispose() {
		synchronized (lock) {
			if (disposed)
				return;

			if (subNetwork != null) {
				subNetwork = null;
			}

			nodeMap.clear();
			edgeMap.clear();

			disposed = true;
		}
	}
}