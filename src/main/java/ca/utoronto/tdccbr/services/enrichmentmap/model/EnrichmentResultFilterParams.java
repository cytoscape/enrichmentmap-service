package ca.utoronto.tdccbr.services.enrichmentmap.model;

public interface EnrichmentResultFilterParams {

//	public static enum NESFilter {
//		ALL, 
//		POSITIVE, 
//		NEGATIVE
//	}
	
	double getPvalue();
	
	double getQvalue();
	
//	NESFilter getNESFilter();
	
//	Optional<Integer> getMinExperiments();
	
	boolean isFDR();
}
