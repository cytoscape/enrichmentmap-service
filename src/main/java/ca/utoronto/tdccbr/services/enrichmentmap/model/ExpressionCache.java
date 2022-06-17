package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ExpressionCache {
	
	private final Cache<Triple<Integer, EMDataSet, Transform>, Optional<float[]>> cache;

	public ExpressionCache() {
		this.cache = CacheBuilder.newBuilder().maximumSize(20).build();
	}

	public Optional<float[]> getExpressions(int geneID, EMDataSet dataset, Transform transform) {
		try {
			return cache.get(Triple.of(geneID, dataset, transform),
					() -> Optional.ofNullable(getExpression(geneID, dataset, transform)));
		} catch (ExecutionException e) {
			return Optional.empty();
		}
	}

	public float getExpression(int geneID, EMDataSet dataset, Transform transform, int expressionIndex) {
		var vals = getExpressions(geneID, dataset, transform);

		return vals.isPresent() ? vals.get()[expressionIndex] : Float.NaN;
	}

	public static GeneExpression getGeneExpression(int geneID, EMDataSet dataset) {
		var matrix = dataset.getExpressionSets();
		var expressions = matrix.getExpressionMatrix();
		var row = expressions.get(geneID);

		return row;
	}

	private static @Nullable float[] getExpression(int geneID, EMDataSet dataset, Transform transform) {
		var expression = getGeneExpression(geneID, dataset);

		if (expression != null) {
			switch (transform) {
				case ROW_NORMALIZE:
					return expression.rowNormalize();
				case LOG_TRANSFORM:
					return expression.rowLogTransform();
				case AS_IS:
					return expression.getExpression();
			}
		}

		return null;
	}
}
