package ca.utoronto.tdccbr.services.enrichmentmap.dto;

public class ResultDTO {

	private EMCreationParametersDTO parameters;
	private ClusterLabelsDTO clusterLabels;
	private NetworkDTO network;
	private NetworkDTO summaryNetwork;
	
	public ResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public ResultDTO(EMCreationParametersDTO parameters, ClusterLabelsDTO clusterLabels, NetworkDTO network, NetworkDTO summaryNetwork) {
		this.parameters = parameters;
		this.clusterLabels = clusterLabels;
		this.network = network;
		this.summaryNetwork = summaryNetwork;
	}
	
	public ResultDTO(EMCreationParametersDTO parameters, ClusterLabelsDTO clusterLabels, NetworkDTO network) {
		this(parameters, clusterLabels, network, null);
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

	public ClusterLabelsDTO getClusterLabels() {
		return clusterLabels;
	}

	public void setClusterLabels(ClusterLabelsDTO clusterLabels) {
		this.clusterLabels = clusterLabels;
	}
}
