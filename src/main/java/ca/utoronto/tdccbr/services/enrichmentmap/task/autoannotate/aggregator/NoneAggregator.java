package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class NoneAggregator implements Aggregator<Object> {

	@Override
	public Class<?> getSupportedType() {
		return null;
	}

	@Override
	public Object aggregate(CyTable table, List<CyNode> nodes, CyColumn column) {
		return null;
	}
}
