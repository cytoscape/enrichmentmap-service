package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class NoneAggregator implements Aggregator<Object> {

	@Override
	public Class<?> getSupportedType() {
		return null;
	}

	@Override
	public Object aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column) {
		return null;
	}
}
