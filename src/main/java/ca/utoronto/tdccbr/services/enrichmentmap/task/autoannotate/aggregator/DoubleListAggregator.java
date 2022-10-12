package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate.aggregator;

import java.util.ArrayList;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class DoubleListAggregator implements Aggregator<List<Double>> {
	
	public static enum Operator { 
		NONE, AVG, MIN, MAX, MEDIAN, SUM, CONCAT, UNIQUE 
	};

	private final Operator op;
	
	public DoubleListAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<?> getSupportedType() {
		return List.class;
	}

	@Override
	public Class<Double> getSupportedListType() {
		return Double.class;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public List<Double> aggregate(CyTable table, List<CyNode> nodes, CyColumn column) {
		Class<?> listType = column.getListElementType();
		List<Double> agg = new ArrayList<Double>();
		List<List<Double>> aggMed = new ArrayList<>();
		Set<Double> aggset = new HashSet<Double>();
		List<Double> aggregation = null;

		if (op == Operator.NONE)
			return null;
		if (!listType.equals(Double.class))
			return null;

		// Initialization

		// Loop processing
		int nodeCount = 0;
		for (CyNode node : nodes) {
			List<?> list = table.getRow(node.getID()).getList(column.getName(), listType);
			if (list == null)
				continue;
			int index = 0;
			nodeCount++;
			for (Object obj : list) {
				Double value = (Double) obj;
				switch (op) {
				case CONCAT:
					agg.add(value);
					break;
				case UNIQUE:
					aggset.add(value);
					break;
				case AVG:
				case SUM:
					if (agg.size() > index) {
						value = value + agg.get(index);
						agg.set(index, value);
					} else {
						agg.add(index, value);
					}
					break;
				case MIN:
					if (agg.size() > index) {
						value = Math.min(value, agg.get(index));
						agg.set(index, value);
					} else {
						agg.add(index, value);
					}
					break;
				case MAX:
					if (agg.size() > index) {
						value = Math.max(value, agg.get(index));
						agg.set(index, value);
					} else {
						agg.add(index, value);
					}
					break;
				case MEDIAN:
					if (aggMed.size() > index) {
						aggMed.get(index).add(value);
					} else {
						List<Double> l = new ArrayList<>();
						l.add(value);
						aggMed.add(index, l);
					}
					break;
				}
				index++;
			}
		}

		if (op == Operator.UNIQUE)
			aggregation = new ArrayList<Double>(aggset);
		else if (op == Operator.AVG) {
			aggregation = new ArrayList<Double>();
			for (Double v : agg) {
				aggregation.add(v / (double) nodeCount);
			}
		} else if (op == Operator.MEDIAN) {
			aggregation = new ArrayList<Double>();
			for (List<Double> valueList : aggMed) {
				Double[] vArray = new Double[valueList.size()];
				vArray = valueList.toArray(vArray);
				Arrays.sort(vArray);
				if (vArray.length % 2 == 1)
					aggregation.add(vArray[(vArray.length - 1) / 2]);
				else
					aggregation.add((vArray[(vArray.length / 2) - 1] + vArray[(vArray.length / 2)]) / 2);
			}
		} else {
			// CONCAT, SUM, MIN, MAX
			aggregation = agg;
		}

		return aggregation;
	}
}
