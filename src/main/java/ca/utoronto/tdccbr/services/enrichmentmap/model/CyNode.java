package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Objects;
import java.util.UUID;

public class CyNode {

	private final String id;
	
	public CyNode() {
		this(UUID.randomUUID().toString());
	}
	
	public CyNode(String id) {
		assert id != null;
		
		this.id = id;
	}
	
	public String getId() {
		return id;
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
		
		var other = (CyNode) obj;
		
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CyNode [id=" + id + "]";
	}
}
