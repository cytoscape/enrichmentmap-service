package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.List;
import java.util.Objects;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet.Method;

public class DataSetParametersDTO {

	private String name;
	private transient Method method;
	private List<FGSEAEnrichmentResultDTO> fgseaResults;
	
	public DataSetParametersDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public DataSetParametersDTO(String name, List<FGSEAEnrichmentResultDTO> fgseaResults) {
		this.name = Objects.requireNonNull(name);
		this.method = Method.FGSEA;
		this.fgseaResults = Objects.requireNonNull(fgseaResults);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}

	public List<FGSEAEnrichmentResultDTO> getFgseaResults() {
		return fgseaResults;
	}
	
	public void setFgseaResults(List<FGSEAEnrichmentResultDTO> fgseaResults) {
		this.fgseaResults = fgseaResults;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		var other = (DataSetParametersDTO) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "DataSetParametersDTO [name=" + name + "]";
	}
}
