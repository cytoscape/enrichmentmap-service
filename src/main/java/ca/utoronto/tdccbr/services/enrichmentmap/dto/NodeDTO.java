package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.Objects;

public class NodeDTO {

	private NodeDataDTO data;
	
	public NodeDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public NodeDTO(NodeDataDTO data) {
		assert data != null;
		
		this.data = data;
	}
	
	public NodeDTO(String id) {
		this.data = new NodeDataDTO(id);
	}
	
	public NodeDataDTO getData() {
		return data;
	}
	
	public void setData(NodeDataDTO data) {
		this.data = data;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		var other = (NodeDTO) obj;
		
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		return "NodeDTO [data=" + data + "]";
	}
}
