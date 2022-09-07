package ca.utoronto.tdccbr.services.enrichmentmap.model.network;

import java.util.Objects;
import java.util.UUID;

public class CyNode implements CyIdentifiable {

	private final UUID id;
	
	public CyNode() {
		this.id = UUID.randomUUID();
	}
	
	@Override
	public UUID getID() {
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
