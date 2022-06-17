package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ca.utoronto.tdccbr.services.enrichmentmap.model.CyTable.CyRow;

public class CyNetwork {
	
	/**
	 * A String column created by default for every CyNetwork that
	 * holds the name of the entry.
	 */
	public static final String NAME = "name";
	public static final String NODE_TABLE = "NODE_TABLE";
	public static final String EDGE_TABLE = "EDGE_TABLE";
	public static final String TABLE_PK = "id";
	
	private final Map<String, NodePointer> nodePointers = new HashMap<>();
	private final Map<String, EdgePointer> edgePointers = new HashMap<>();

	private int nodeCount;
	private int edgeCount;
	
	private NodePointer firstNode;
	
	private final CyTable nodeTable = new CyTable(NODE_TABLE, TABLE_PK, String.class);
	private final CyTable edgeTable = new CyTable(EDGE_TABLE, TABLE_PK, String.class);
	
	private Object lock = new Object();
	
	public CyNetwork() {
		// Create standard columns
		nodeTable.createColumn(NAME, String.class);
		
		edgeTable.createColumn(NAME, String.class);
		edgeTable.createColumn(CyEdge.INTERACTION, String.class);
	}
	
	public int getNodeCount() {
		synchronized (lock) {
			return nodeCount;
		}
	}

	public int getEdgeCount() {
		synchronized (lock) {
			return edgeCount;
		}
	}

	public CyEdge getEdge(String id) {
		synchronized (lock) {
			var ep = edgePointers.get(id);
			
			if (ep != null)
				return ep.cyEdge;
			else
				return null;
		}
	}

	public CyNode getNode(String id) {
		synchronized (lock) {
			var np = nodePointers.get(id);
			
			if (np != null)
				return np.cyNode;
			else
				return null;
		}
	}

	public List<CyNode> getNodeList() {
		synchronized (lock) {
			var ret = new ArrayList<CyNode>(nodeCount);
			int numRemaining = nodeCount;
			var node = firstNode;
	
			while (numRemaining > 0) {
				// possible NPE here if the linked list isn't constructed correctly
				// this is the correct behavior
				var toAdd = node.cyNode;
				node = node.nextNode;
				ret.add(toAdd);
				numRemaining--;
			}
	
			return ret;
		}
	}
	
	public CyNode addNode() {
		var node = new CyNode();
		
		if (containsNode(node))
			return node;
		
		synchronized (lock) {
			var np = new NodePointer(node);
			nodePointers.put(node.getId(), np);
			nodeCount++;
			firstNode = np.insert(firstNode);
		}
		
		return node;
	}
	
	public CyEdge addEdge(CyNode source, CyNode target) {
		var edge = new CyEdge(source, target);
		
		final EdgePointer ep;

		synchronized (lock) {
			if (!containsNode(edge.getSource()))
				throw new IllegalArgumentException("source node is not a member of this network");
			if (!containsNode(edge.getTarget()))
				throw new IllegalArgumentException("target node is not a member of this network");

			// Edge already exists in this network?
			if (containsEdge(edge))
				return edge;

			var src = getNodePointer(edge.getSource());
			var tgt = getNodePointer(edge.getTarget());

			ep = new EdgePointer(src, tgt, edge);

			edgePointers.put(edge.getId(), ep);
			edgeCount++;
		}

		return edge; 
	}

	public List<CyEdge> getEdgeList() {
		synchronized (lock) {
			var ret = new ArrayList<CyEdge>(edgeCount);
			EdgePointer edge = null;
	
			int numRemaining = edgeCount;
			var node = firstNode;
			
			while (numRemaining > 0) {
				final CyEdge retEdge;
	
				if (edge != null) {
					retEdge = edge.cyEdge;
				} else {
					for (edge = node.firstOutEdge; 
					     edge == null; 
					     node = node.nextNode, edge = node.firstOutEdge);
	
					node = node.nextNode;
					retEdge = edge.cyEdge;
				}
	
				edge = edge.nextOutEdge;
				numRemaining--;
	
				ret.add(retEdge);
			}
	
			return ret;
		}
	}

	public List<CyNode> getNeighborList(CyNode n, CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList(); 
	
			var np = getNodePointer(n);
			var ret = new ArrayList<CyNode>(countEdges(np, e));
			var it = edgesAdjacent(np, e);
			
			while (it.hasNext()) {
				var edge = it.next();
				var neighborId = np.id.equals(edge.source.id) ? edge.target.id : edge.source.id;
				ret.add(getNode(neighborId));
			}
	
			return ret;
		}
	}

	public List<CyEdge> getAdjacentEdgeList(CyNode n, CyEdge.Type type) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList(); 
	
			var np = getNodePointer(n);
			var ret = new ArrayList<CyEdge>(countEdges(np, type));
			var it = edgesAdjacent(np, type);
	
			while (it.hasNext()) {
				ret.add(it.next().cyEdge);
			}
	
			return ret;
		}
	}

	public Iterable<CyEdge> getAdjacentEdgeIterable(CyNode n, CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList();
	
			var np = getNodePointer(n);
			return new IterableEdgeIterator( edgesAdjacent(np, e) ); 
		}
	}

	private class IterableEdgeIterator implements Iterator<CyEdge>, Iterable<CyEdge> {
		
		private final Iterator<EdgePointer> epIterator;
		
		IterableEdgeIterator(Iterator<EdgePointer> epIterator) {
			this.epIterator = epIterator;
		}
		
		@Override
		public CyEdge next() {
			return epIterator.next().cyEdge;
		}

		@Override
		public boolean hasNext() {
			return epIterator.hasNext();
		}

		@Override
		public void remove() {
			epIterator.remove();
		}

		@Override
		public Iterator<CyEdge> iterator() {
			return this;
		}
	}

	public List<CyEdge> getConnectingEdgeList(CyNode src, CyNode trg, CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(src)) 
				return Collections.emptyList(); 
	
			if (!containsNode(trg)) 
				return Collections.emptyList(); 
	
			var srcP = getNodePointer(src);
			var trgP = getNodePointer(trg);
	
			var ret = new ArrayList<CyEdge>(Math.min(countEdges(srcP, e), countEdges(trgP, e)));
			var it = edgesConnecting(srcP, trgP, e);
	
			while (it.hasNext())
				ret.add(it.next().cyEdge);
	
			return ret;
		}
	}

	protected boolean removeNodesInternal(Collection<CyNode> nodes) {
		if (nodes == null || nodes.isEmpty())
			return false;

		boolean madeChanges = false;
		
		synchronized (lock) {
			for (var n : nodes) {
				if (!containsNode(n))
					continue;

				// remove adjacent edges from network
				removeEdgesInternal(getAdjacentEdgeList(n, CyEdge.Type.ANY));
	
				var node = nodePointers.remove(n.getId());
				firstNode = node.remove(firstNode);
	
				nodeCount--;
				madeChanges = true;
			}
		}

		return madeChanges;
	}

	protected boolean removeEdgesInternal(Collection<CyEdge> edges) {
		if (edges == null || edges.isEmpty())
			return false;

		boolean madeChanges = false;
		
		synchronized (lock) {
			for (var edge : edges) {
				if (!containsEdge(edge))
					continue;
	
				var e = edgePointers.remove(edge.getId());
	
				e.remove();
	
				edgeCount--;
				madeChanges = true;
			}
		}

		return madeChanges;
	}

	public boolean containsNode(CyNode node) {
		if (node == null)
			return false;

		final NodePointer thisNode; 

		synchronized (lock) {
			thisNode = nodePointers.get(node.getId());
		}

		if (thisNode == null)
			return false;

		return thisNode.cyNode.equals(node);
	}

	public boolean containsEdge(CyEdge edge) {
		if (edge == null)
			return false;

		final EdgePointer thisEdge; 

		synchronized (lock) {
			thisEdge = edgePointers.get(edge.getId());
		}

		if (thisEdge == null)
			return false;

		return thisEdge.cyEdge.equals(edge);
	}

	public boolean containsEdge(CyNode n1, CyNode n2) {
		synchronized (lock) {
			if (!containsNode(n1))
				return false;
	
			if (!containsNode(n2))
				return false;
	
			var it = edgesConnecting(getNodePointer(n1), getNodePointer(n2), CyEdge.Type.ANY);
	
			return it.hasNext();
		}
	}
	
	public CyTable getNodeTable() {
		return nodeTable;
	}
	
	public CyTable getEdgeTable() {
		return edgeTable;
	}
	
	public CyRow getRow(CyNode node) {
		if (node == null)
			throw new IllegalArgumentException("'node' must not be null");
		
		return nodeTable.getRow(node.getId());
	}
	
	public CyRow getRow(CyEdge edge) {
		if (edge == null)
			throw new IllegalArgumentException("'edge' must not be null");
		
		return edgeTable.getRow(edge.getId());
	}

	private Iterator<EdgePointer> edgesAdjacent(NodePointer n, CyEdge.Type edgeType) {
		assert (n != null);

		final EdgePointer[] edgeLists;

		boolean incoming = assessIncoming(edgeType);
		boolean outgoing = assessOutgoing(edgeType);
		boolean undirected = assessUndirected(edgeType);

		if (undirected || (outgoing && incoming)) 
			edgeLists = new EdgePointer[] { n.firstOutEdge, n.firstInEdge };
		else if (outgoing) // Cannot also be incoming.
			edgeLists = new EdgePointer[] { n.firstOutEdge, null };
		else if (incoming) // Cannot also be outgoing.
			edgeLists = new EdgePointer[] { null, n.firstInEdge };
		else // All boolean input parameters are false - can never get here!
			edgeLists = new EdgePointer[] { null, null };

		int inEdgeCount = countEdges(n, edgeType);
		//System.out.println("edgesAdjacent edgeCount: " + inEdgeCount);

		return new Iterator<EdgePointer>() {
				private int numRemaining = inEdgeCount;
				private int edgeListIndex = -1;
				private EdgePointer edge;

				@Override
				public boolean hasNext() {
					return numRemaining > 0;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public EdgePointer next() {
					// get the first non-null edgePointer
					while (edge == null)
						edge = edgeLists[++edgeListIndex];

					String returnId = null;

					// look at outgoing edges
					if (edgeListIndex == 0) {
						// go to the next edge if the current edge is NOT either
						// directed when we want outgoing or undirected when we want undirected
						while ((edge != null) && 
						       !((outgoing && edge.directed) || (undirected && !edge.directed))) {
							edge = edge.nextOutEdge;

							// we've hit the last edge in the list
							// so increment edgeListIndex so we go to 
							// incoming, set edge, and break
							if (edge == null) {
								edge = edgeLists[++edgeListIndex];
								break;
							}
						}
					
						// if we have a non-null outgoing edge set the 
						// edge and return values
						// since edgeListIndex is still for outgoing we'll
						// just directly to the return
						if ((edge != null) && (edgeListIndex == 0)) {
							returnId = edge.id;
							edge = edge.nextOutEdge;
						}
					}
	
					// look at incoming edges
					if (edgeListIndex == 1) {
						// Important NOTE!!!
						// Possible null pointer exception here if numRemaining, 
						// i.e. edgeCount is wrong. However, this is probably the
						// correct behavior since it means the linked lists are
						// messed up and there isn't a graceful way to deal.


						// go to the next edge if the edge is a self edge AND 
						// either directed when we're looking for outgoing or
						// undirected when we're looking for undirected 
						// OR 
						// go to the next edge if the current edge is NOT either
						// directed when we want incoming or undirected when we
						// want undirected
						while ((Objects.equals(edge.source.id, edge.target.id)
						       && ((outgoing && edge.directed) || (undirected && !edge.directed)))
						       || !((incoming && edge.directed) || (undirected && !edge.directed))) {
							edge = edge.nextInEdge;
						}

						returnId = edge.id;
						edge = edge.nextInEdge;
					}

					numRemaining--;
					return edgePointers.get(returnId);
				}
			};
	}

	private Iterator<EdgePointer> edgesConnecting(NodePointer node0, NodePointer node1, CyEdge.Type et) {
		assert node0 != null;
		assert node1 != null;

		final Iterator<EdgePointer> theAdj;
		final String nodeZero;
		final String nodeOne;

		// choose the smaller iterator
		if (countEdges(node0, et) <= countEdges(node1, et)) {
			theAdj = edgesAdjacent(node0, et);
			nodeZero = node0.id;
			nodeOne = node1.id;
		} else {
			theAdj = edgesAdjacent(node1, et);
			nodeZero = node1.id;
			nodeOne = node0.id;
		}

		return new Iterator<EdgePointer>() {
				private String nextIndex;

				private void ensureComputeNext() {
					if (nextIndex == null)
						return;

					while (theAdj.hasNext()) {
						var e = theAdj.next();
						var id = nodeZero.equals(e.source.id) ? e.target.id : e.source.id;

						if (id.equals(nodeOne)) {
							nextIndex = e.id;

							return;
						}
					}

					nextIndex = null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean hasNext() {
					ensureComputeNext();

					return nextIndex != null;
				}

				@Override
				public EdgePointer next() {
					ensureComputeNext();

					var returnIndex = nextIndex;
					nextIndex = null;

					return edgePointers.get(returnIndex);
				}
			};
	}

	private boolean assessUndirected(CyEdge.Type e) {
		return e == CyEdge.Type.UNDIRECTED || e == CyEdge.Type.ANY;
	}

	private boolean assessIncoming(CyEdge.Type e) {
		return e == CyEdge.Type.DIRECTED || e == CyEdge.Type.ANY || e == CyEdge.Type.INCOMING;
	}

	private boolean assessOutgoing(CyEdge.Type e) {
		return e == CyEdge.Type.DIRECTED || e == CyEdge.Type.ANY || e == CyEdge.Type.OUTGOING;
	}

	private int countEdges(NodePointer n, CyEdge.Type edgeType) {
		boolean undirected = assessUndirected(edgeType);
		boolean incoming = assessIncoming(edgeType);
		boolean outgoing = assessOutgoing(edgeType);

		int count = 0;

		if (outgoing)
			count += n.outDegree;
		if (incoming)
			count += n.inDegree;
		if (undirected)
			count += n.undDegree;

		if (outgoing && incoming)
			count -= n.selfEdges;

		return count;
	}

	private NodePointer getNodePointer(CyNode node) {
		return nodePointers.get(node.getId());
	}
}
