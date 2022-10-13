package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class ListAggregator implements Aggregator<List> {
	
	public static enum Operator { 
		NONE, CONCAT, UNIQUE 
	};

	private final Operator op;
	
	public ListAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class getSupportedType() {
		return List.class;
	}

	@Override
	public List aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column) {
		Class listType = column.getListElementType();
		List<Object> agg = new ArrayList<Object>();
		Set<Object> aggset = new HashSet<Object>();
		List<?> aggregation = null;

		if (op == Operator.NONE)
			return null;

		// Initialization

		// Loop processing
		for (var ele : eles) {
			List<Object> list = table.getRow(ele.getID()).getList(column.getName(), listType);
			if (list == null)
				continue;
			for (Object value : list) {
				switch (op) {
				case CONCAT:
					agg.add(value);
					break;
				case UNIQUE:
					aggset.add(value);
					break;
				}
			}
		}

		if (op == Operator.CONCAT)
			aggregation = agg;
		else if (op == Operator.UNIQUE)
			aggregation = new ArrayList<Object>(aggset);

		return aggregation;
	}
}
