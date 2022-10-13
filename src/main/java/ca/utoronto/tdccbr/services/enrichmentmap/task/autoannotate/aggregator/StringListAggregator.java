package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class StringListAggregator implements Aggregator<List<String>> {
	
	public static enum Operator { 
		NONE, CONCAT, UNIQUE 
	};

	private final Operator op;
	
	public StringListAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<?> getSupportedType() {
		return List.class;
	}

	@Override
	public Class<?> getSupportedListType() {
		return String.class;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public List<String> aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column) {
		Class<?> listType = column.getListElementType();
		List<String> agg = new ArrayList<String>();
		Set<String> aggset = new HashSet<String>();
		List<String> aggregation = null;

		if (op == Operator.NONE)
			return null;
		if (!listType.equals(String.class))
			return null;

		// Initialization

		// Loop processing
		for (var ele : eles) {
			List<?> list = table.getRow(ele.getID()).getList(column.getName(), listType);
			if (list == null)
				continue;
			for (Object obj : list) {
				String value = (String) obj;
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
			aggregation = new ArrayList<String>(aggset);

		return aggregation;
	}
}
