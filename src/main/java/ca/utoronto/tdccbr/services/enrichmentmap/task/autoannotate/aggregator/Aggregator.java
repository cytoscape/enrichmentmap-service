package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public interface Aggregator<T> {

	public Class<?> getSupportedType();
	
	default Class<?> getSupportedListType() {
		return null;
	}

	public T aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column);

}
