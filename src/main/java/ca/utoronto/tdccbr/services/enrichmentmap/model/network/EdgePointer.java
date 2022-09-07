package ca.utoronto.tdccbr.services.enrichmentmap.model.network;

import java.util.UUID;

/**
 * Element of the edge linked list used in {@link SimpleNetwork}.
 * You should only touch this if you know what you're doing.
 */
final class EdgePointer {

	final CyEdge cyEdge;
	final UUID id;
	final NodePointer source;
	final NodePointer target;
	final boolean directed;

	EdgePointer nextOutEdge;
	EdgePointer prevOutEdge;
	EdgePointer nextInEdge;
	EdgePointer prevInEdge;

	EdgePointer(NodePointer s, NodePointer t, CyEdge edge) {
		source = s;
		target = t;
		cyEdge = edge;
		id = edge.getID();
		directed = edge.isDirected();

		nextOutEdge = null;
		prevOutEdge = null;

		nextInEdge = null;
		prevInEdge = null;

		insertSelf();
	}

	private void insertSelf() {
		nextOutEdge = source.firstOutEdge;

		if (source.firstOutEdge != null)
			source.firstOutEdge.prevOutEdge = this;

		source.firstOutEdge = this;

		nextInEdge = target.firstInEdge;

		if (target.firstInEdge != null)
			target.firstInEdge.prevInEdge = this;

		target.firstInEdge = this;

		if (cyEdge.isDirected()) {
			source.outDegree++;
			target.inDegree++;
		} else {
			source.undDegree++;
			target.undDegree++;
		}

		// Self-edge
		if (source == target)
			source.selfEdges++;
	}

	void remove() {
		if (prevOutEdge != null)
			prevOutEdge.nextOutEdge = nextOutEdge;
		else
			source.firstOutEdge = nextOutEdge;

		if (nextOutEdge != null)
			nextOutEdge.prevOutEdge = prevOutEdge;

		if (prevInEdge != null)
			prevInEdge.nextInEdge = nextInEdge;
		else
			target.firstInEdge = nextInEdge;

		if (nextInEdge != null)
			nextInEdge.prevInEdge = prevInEdge;

		if (cyEdge.isDirected()) {
			source.outDegree--;
			target.inDegree--;
		} else {
			source.undDegree--;
			target.undDegree--;
		}

		// Self-edge.
		if (source == target)
			source.selfEdges--;

		nextOutEdge = null; // ?? wasn't here in DynamicGraph
		prevOutEdge = null;
		nextInEdge = null;
		prevInEdge = null;
	}
}
