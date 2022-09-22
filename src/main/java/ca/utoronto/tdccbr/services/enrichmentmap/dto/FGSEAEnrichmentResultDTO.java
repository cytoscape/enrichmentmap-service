package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.Objects;

public class FGSEAEnrichmentResultDTO {

	private String pathway;
	private int size;
	private double pval = 1.0;
	private double padj = 1.0; // adjusted p-value
	private double ES = 1.0;
	private double NES = 1.0;
	
	public FGSEAEnrichmentResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public FGSEAEnrichmentResultDTO(
			String pathway,
			int size,
			double pval,
			double padj,
			double es,
			double nes
	) {
		this.pathway = pathway;
		this.size = size;
		this.pval = pval;
		this.padj = padj;
		this.ES = es;
		this.NES = nes;
	}

	public String getPathway() {
		return pathway;
	}

	public void setPathway(String pathway) {
		this.pathway = pathway;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public double getPval() {
		return pval;
	}

	public void setPval(double pval) {
		this.pval = pval;
	}

	public double getES() {
		return ES;
	}

	public void setES(double es) {
		ES = es;
	}

	public double getNES() {
		return NES;
	}

	public void setNES(double nes) {
		NES = nes;
	}

	
	public double getPadj() {
		return padj;
	}

	public void setPadj(double padj) {
		this.padj = padj;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pathway);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		var other = (FGSEAEnrichmentResultDTO) obj;
		return Objects.equals(pathway, other.pathway);
	}

	@Override
	public String toString() {
		return "FGSEAEnrichmentResultDTO [pathway=" + pathway + ", size=" + size + ", pval=" + pval + ", padj=" + padj
				+ ", ES=" + ES + ", NES=" + NES + "]";
	}

	
}
