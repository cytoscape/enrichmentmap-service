package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.List;

public class RequestDTO {

	private EMCreationParametersDTO parameters;
	private List<DataSetParametersDTO> dataSets;
	
	public RequestDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public RequestDTO(EMCreationParametersDTO parameters, List<DataSetParametersDTO> dataSets) {
		this.parameters = parameters;
		this.dataSets = dataSets;
	}

	public EMCreationParametersDTO getParameters() {
		return parameters;
	}
	
	public void setParameters(EMCreationParametersDTO parameters) {
		this.parameters = parameters;
	}
	
	public List<DataSetParametersDTO> getDataSets() {
		return dataSets;
	}
	
	public void setDataSets(List<DataSetParametersDTO> dataSets) {
		this.dataSets = dataSets;
	}
}
