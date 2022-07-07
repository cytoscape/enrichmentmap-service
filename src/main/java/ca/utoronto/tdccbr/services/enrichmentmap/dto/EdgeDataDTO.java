package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdgeDataDTO {

	private String id;
	private String source;
	private String target;
	private double similarityCoefficient;
	private int overlapSize;
//	private List<String> overlapGenes;

	public EdgeDataDTO() {
		// We need this zero-argument constructor because of the JavaBean standard...
	}
	
	public EdgeDataDTO(String id, String source, String target) {
		assert id != null && !id.isBlank();
		assert source != null && !source.isBlank();
		assert target != null && !target.isBlank();
		
		this.id = id;
		this.source = source;
		this.target = target;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@JsonProperty("similarity_coefficient")
	public double getSimilarityCoefficient() {
		return similarityCoefficient;
	}

	public void setSimilarityCoefficient(double similarityCoefficient) {
		this.similarityCoefficient = similarityCoefficient;
	}

	@JsonProperty("overlap_size")
	public int getOverlapSize() {
		return overlapSize;
	}

	public void setOverlapSize(int overlapSize) {
		this.overlapSize = overlapSize;
	}

//	@JsonProperty("overlap_genes")
//	public List<String> getOverlapGenes() {
//		return overlapGenes;
//	}
//
//	public void setOverlapGenes(List<String> overlapGenes) {
//		this.overlapGenes = overlapGenes;
//	}

	@Override
	public String toString() {
		return "EdgeDataDTO [id=" + id + ", source=" + source + ", target=" + target + "]";
	}
}
