package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.Objects;

public class EdgeDTO {

	private EdgeDataDTO data;
	
	public EdgeDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public EdgeDTO(EdgeDataDTO data) {
		assert data != null;
		
		this.data = data;
	}
	
	public EdgeDTO(String id, String source, String target) {
		this.data = new EdgeDataDTO(id, source, target);
	}

	public EdgeDataDTO getData() {
		return data;
	}
	
	public void setData(EdgeDataDTO data) {
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
		
		var other = (EdgeDTO) obj;
		
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		return "EdgeDTO [data=" + data + "]";
	}
}
