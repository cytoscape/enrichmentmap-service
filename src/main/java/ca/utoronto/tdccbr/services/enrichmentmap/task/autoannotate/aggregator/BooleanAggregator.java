package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class BooleanAggregator implements Aggregator<Boolean> {
	
	public static enum Operator { 
		NONE, AND, OR 
	};
	
	private final Operator op;

	public BooleanAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<Boolean> getSupportedType() {
		return Boolean.class;
	}

	@Override
	public Boolean aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column) {
		if (op == Operator.NONE)
			return null;

		// Initialization
		boolean aggregation = false;
		boolean first = true;

		// Loop processing
		for (var ele : eles) {
			Boolean v = table.getRow(ele.getID()).get(column.getName(), Boolean.class);
			if (v == null)
				continue;
			boolean value = v.booleanValue();
			if (first) {
				aggregation = value;
				first = false;
				continue;
			}

			switch (op) {
				case AND:
					aggregation = aggregation & value;
					break;
				case OR:
					aggregation = aggregation | value;
					break;
				default:
					return null;
			}
		}

		return Boolean.valueOf(aggregation);
	}
}
