package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Optional;

/**
 * Common interface for different levels of compression.
 */
public interface ExpressionData {
	
	EMDataSet getDataSet(int col);

	double getValue(int geneID, int col, Compress compress, Transform transform);

	String getName(int col);

	public default Optional<String> getPhenotype(int col) {
		return Optional.empty();
	};

	int getSize();
}
