package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.List;
import java.util.Objects;

public class FGSEAEnrichmentResultDTO {

	private String pathway;
	private int size;
	private double pval = 1.0;
	private double ES;
	private double NES = 1.0;
	private List<String> leadingEdge;
	private List<String> genes;
	
	public FGSEAEnrichmentResultDTO() {
		// Zero-argument constructor for the JavaBean standard...
	}
	
	public FGSEAEnrichmentResultDTO(
			String pathway,
			int size,
			double pval,
			double es,
			double nes,
			List<String> leadingEdge,
			List<String> genes
	) {
		this.pathway = pathway;
		this.size = size;
		this.pval = pval;
		this.ES = es;
		this.NES = nes;
		this.leadingEdge = leadingEdge;
		this.genes = genes;
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

	public List<String> getLeadingEdge() {
		return leadingEdge;
	}

	public void setLeadingEdge(List<String> leadingEdge) {
		this.leadingEdge = leadingEdge;
	}

	public List<String> getGenes() {
		return genes;
	}

	public void setGenes(List<String> genes) {
		this.genes = genes;
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
		return "FGSEAPathwayDTO [pathway=" + pathway + ", size=" + size + ", pval=" + pval + ", ES=" + ES + ", NES=" + NES + "]";
	}
}
