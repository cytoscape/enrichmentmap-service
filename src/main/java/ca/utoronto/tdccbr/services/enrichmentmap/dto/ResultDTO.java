package ca.utoronto.tdccbr.services.enrichmentmap.dto;

public class ResultDTO {

	private EMCreationParametersDTO parameters;
	private ClusterLabelsDTO clusterLabels;
	private NetworkDTO network;
	
	public ResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public ResultDTO(EMCreationParametersDTO parameters, NetworkDTO network, ClusterLabelsDTO clusterLabels) {
		this.parameters = parameters;
		this.clusterLabels = clusterLabels;
		this.network = network;
	}

	public NetworkDTO getNetwork() {
		return network;
	}
	
	public void setNetwork(NetworkDTO network) {
		this.network = network;
	}
	
	public EMCreationParametersDTO getParameters() {
		return parameters;
	}
	
	public void setParameters(EMCreationParametersDTO parameters) {
		this.parameters = parameters;
	}

	public ClusterLabelsDTO getClusterLabels() {
		return clusterLabels;
	}

	public void setClusterLabels(ClusterLabelsDTO clusterLabels) {
		this.clusterLabels = clusterLabels;
	}
}
