package ca.utoronto.tdccbr.services.enrichmentmap.model.network;

import java.util.Objects;
import java.util.UUID;

public class CyEdge implements CyIdentifiable {

	/**
	 * A String column created by default for every CyEdge that
	 * holds the interaction description of the edge. 
	 */
	public static final String INTERACTION = "interaction";
	
	/**
	 * The Type enum is used by methods in {@link CyNetwork} to restrict
	 * the edges that match a query. 
	 */
	public static enum Type {

		/**
		 * matches only undirected edges
		 */
		UNDIRECTED,

		/**
		 * matches either undirected edges or directed edges that end with this node</li>
		 */
		INCOMING,

		/**
		 * matches either undirected edges or directed edges that start with this node</li>
		 */
		OUTGOING,

		/**
		 * matches directed edges regardless of whether this node is the source or the target
		 */
		DIRECTED,

		/**
	 	 * matches any edge
		 */
		ANY;
	}
	
	private final UUID id;
	private final CyNode source;
	private final CyNode target;
	private final boolean directed;
	
	public CyEdge(CyNode source, CyNode target) {
		this(UUID.randomUUID(), source, target, false);
	}
	
	private CyEdge(UUID id, CyNode source, CyNode target, boolean directed) {
		assert id != null;
		assert source != null;
		assert target != null;
		
		this.id = id;
		this.source = source;
		this.target = target;
		this.directed = directed;
	}
	
	@Override
	public UUID getID() {
		return id;
	}
	
	public CyNode getSource() {
		return source;
	}
	
	public CyNode getTarget() {
		return target;
	}
	
	public boolean isDirected() {
		return directed;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		var other = (CyEdge) obj;
		
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CyEdge [id=" + id + ", source=" + source + ", target=" + target + ", directed=" + directed + "]";
	}
}
