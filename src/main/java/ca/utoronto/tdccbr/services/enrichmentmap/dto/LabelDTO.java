package ca.utoronto.tdccbr.services.enrichmentmap.dto;

public class LabelDTO {
	
	private String clusterId;
	private String label;
	
	public LabelDTO() {
	}
	
	public LabelDTO(String clusterId, String label) {
		this.clusterId = clusterId;
		this.label = label;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
