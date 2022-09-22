package ca.utoronto.tdccbr.services.enrichmentmap.model;

/**
 * Note: The FGSEA service could return the leading edge. 
 * There is no need to recalcualte it like there is with GSEA.
 */
public class FGSEAResult extends EnrichmentResult {
	
	private final double padj;
	private final double ES;
	private final double NES;
	
	public FGSEAResult(String name, String desc, double pvalue, int gsSize, double padj, double ES, double NES) {
		super(name, desc, pvalue, gsSize);
		this.padj = padj;
		this.ES = ES;
		this.NES = NES;
	}

	public double getPadj() {
		return padj;
	}

	public double getES() {
		return ES;
	}

	public double getNES() {
		return NES;
	}
	
	@Override
	public boolean isGeneSetOfInterest(EnrichmentResultFilterParams params) {
		double pvalue = params.getPvalue();
		double fdrqvalue = params.getQvalue();
		boolean useFDR = params.isFDR();
		
		if (useFDR)
			return (getPvalue() <= pvalue) && (this.padj <= fdrqvalue);
		else
			return (getPvalue() <= pvalue);
	}

	@Override
	public String toString() {
		return "FGSEAResult [name=" + getName() + ", size=" + getGsSize() + ", pvalue=" + getPvalue()
				+ ", padj=" + padj + ", ES=" + ES + ", NES=" + NES + "]";
	}

	
	
	
	
}
