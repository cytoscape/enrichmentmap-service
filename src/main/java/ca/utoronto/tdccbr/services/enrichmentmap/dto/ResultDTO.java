package ca.utoronto.tdccbr.services.enrichmentmap.dto;

public class ResultDTO {

	private EMCreationParametersDTO parameters;
	private NetworkDTO network;
	
	public ResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public ResultDTO(EMCreationParametersDTO parameters, NetworkDTO network) {
		this.parameters = parameters;
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
}
