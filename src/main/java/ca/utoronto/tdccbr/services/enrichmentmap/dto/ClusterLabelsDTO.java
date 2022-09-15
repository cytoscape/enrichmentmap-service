package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.ArrayList;
import java.util.List;

public class ClusterLabelsDTO {
	
	private List<LabelDTO> labels;
	

	public ClusterLabelsDTO() {
	}
	
	public ClusterLabelsDTO(List<LabelDTO> labels) {
		this.labels = labels;
	}
	
	public List<LabelDTO> getLabels() {
		if(labels == null)
			labels = new ArrayList<>();
		return labels;
	}

	public void setLabels(List<LabelDTO> labels) {
		this.labels = labels;
	}

}
