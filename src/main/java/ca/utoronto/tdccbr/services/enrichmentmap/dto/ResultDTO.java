package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.List;

public class ResultDTO {

	private EMCreationParametersDTO parameters;
	private List<ClusterLabelsDTO> clusterLabels;
	private NetworkDTO network;
	private NetworkDTO summaryNetwork;
	
	public ResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public ResultDTO(EMCreationParametersDTO parameters, List<ClusterLabelsDTO> clusterLabels, NetworkDTO network, NetworkDTO summaryNetwork) {
		this.parameters = parameters;
		this.clusterLabels = clusterLabels;
		this.network = network;
		this.summaryNetwork = summaryNetwork;
	}
	
	
	public NetworkDTO getNetwork() {
		return network;
	}
	
	public void setNetwork(NetworkDTO network) {
		this.network = network;
	}
	
	public NetworkDTO getSummaryNetwork() {
		return summaryNetwork;
	}

	public void setSummaryNetwork(NetworkDTO summaryNetwork) {
		this.summaryNetwork = summaryNetwork;
	}
	
	public EMCreationParametersDTO getParameters() {
		return parameters;
	}
	
	public void setParameters(EMCreationParametersDTO parameters) {
		this.parameters = parameters;
	}

	public List<ClusterLabelsDTO> getClusterLabels() {
		return clusterLabels;
	}

	public void setClusterLabels(List<ClusterLabelsDTO> clusterLabels) {
		this.clusterLabels = clusterLabels;
	}
}
