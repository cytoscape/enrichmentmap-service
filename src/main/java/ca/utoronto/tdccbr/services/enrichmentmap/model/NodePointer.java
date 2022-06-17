package ca.utoronto.tdccbr.services.enrichmentmap.model;

/**
 * Element of the edge linked list used in {@link ArrayGraph}.
 * You should only touch this if you know what you're doing!
 */
final class NodePointer {
	
	final CyNode cyNode;
	final String id;

	NodePointer nextNode; 
	NodePointer prevNode;
	EdgePointer firstOutEdge; 
	EdgePointer firstInEdge;

	/** The number of directed edges whose source is this node. */
	int outDegree; 

	/** The number of directed edges whose target is this node. */
	int inDegree; 

	/** The number of undirected edges which touch this node. */
	int undDegree; 

	/** The number of directed self-edges on this node. */
	int selfEdges; 

	NodePointer(CyNode cyn) {
		cyNode = cyn;
		id = cyn.getId();

		outDegree = 0;
		inDegree = 0;
		undDegree = 0;
		selfEdges = 0;

		firstOutEdge = null;
		firstInEdge = null;
	}

	NodePointer insert(NodePointer next) {
		nextNode = next;
		
		if (next != null)
			next.prevNode = this;
		
		return this;
	}

	NodePointer remove(NodePointer first) {
		NodePointer ret = first;
		
		if (prevNode != null)
			prevNode.nextNode = nextNode;
		else
			ret = nextNode;

		if (nextNode != null)
			nextNode.prevNode = prevNode;

		nextNode = null;
		prevNode = null;
		firstOutEdge = null;
		firstInEdge = null;

		return ret;
	}
}
