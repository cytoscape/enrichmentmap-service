package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class StringAggregator implements Aggregator<String> {
	
	public static enum Operator { 
		NONE, CSV, TSV, MCV, UNIQUE 
	};

	private final Operator op;
	
	public StringAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<?> getSupportedType() {
		return String.class;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public String aggregate(CyTable table, List<CyNode> nodes, CyColumn column) {
		String aggregation = null;
		Map<String, Integer> histo = null;
		Set<String> unique = null;

		if (op == Operator.NONE)
			return null;

		// Initialization

		// Loop processing
		for (CyNode node : nodes) {
			String value = table.getRow(node.getID()).get(column.getName(), String.class);
			if (value == null)
				continue;

			switch (op) {
			case CSV:
				if (aggregation == null)
					aggregation = value;
				else
					aggregation = aggregation + "," + value;
				break;
			case TSV:
				if (aggregation == null)
					aggregation = value;
				else
					aggregation = aggregation + "\t" + value;
				break;
			case MCV:
				if (histo == null)
					histo = new HashMap<String, Integer>();
				if (histo.containsKey(value))
					histo.put(value, histo.get(value).intValue() + 1);
				else
					histo.put(value, 1);
				break;
			case UNIQUE:
				if (unique == null)
					unique = new HashSet<String>();
				unique.add(value);
				break;
			}
		}

		// Post processing
		if (op == Operator.MCV) {
			int maxValue = -1;
			for (String key : histo.keySet()) {
				int count = histo.get(key);
				if (count > maxValue) {
					aggregation = key;
					maxValue = count;
				}
			}
			if (aggregation == null)
				aggregation = "";
		} else if (op == Operator.UNIQUE) {
			for (String value : unique) {
				if (aggregation == null)
					aggregation = value;
				else
					aggregation += "," + value;
			}
		}

		return aggregation;
	}
}
