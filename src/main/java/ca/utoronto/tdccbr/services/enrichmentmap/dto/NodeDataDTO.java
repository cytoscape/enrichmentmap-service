package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeDataDTO {

	private String id;
	private String name;
	private String gsType;
	private int gsSize;
	private double pvalue;
	private double padj;
	private double NES;
	private String mcodeClusterID;
	
	
	public NodeDataDTO() {
		// We need this zero-argument constructor because of the JavaBean standard...
	}
	
	public NodeDataDTO(String id) {
		assert id != null && !id.isBlank();
		
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("gs_type")
	public String getGsType() {
		return gsType;
	}

	public void setGsType(String gsType) {
		this.gsType = gsType;
	}

	@JsonProperty("gs_size")
	public int getGsSize() {
		return gsSize;
	}

	public void setGsSize(int gsSize) {
		this.gsSize = gsSize;
	}

	public double getPvalue() {
		return pvalue;
	}

	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}

	@JsonProperty("padj")
	public double getPadj() {
		return padj;
	}

	public void setPadj(double padj) {
		this.padj = padj;
	}

	@JsonProperty("NES")
	public double getNES() {
		return NES;
	}

	public void setNES(double nes) {
		NES = nes;
	}

	@JsonProperty("mcode_cluster_id")
	public String getMcodeClusterID() {
		return  mcodeClusterID;
	}
	
	public void setMcodeClusterID(String mcodeClusterID) {
		this.mcodeClusterID = mcodeClusterID;
	}
	
	@Override
	public String toString() {
		return "NodeDataDTO [id=" + id + ", name=" + name + "]";
	}
}
