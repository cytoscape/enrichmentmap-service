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

public class IntegerListAggregator implements Aggregator<List<Integer>> {
	
	public static enum Operator { 
		NONE, AVG, MIN, MAX, MEDIAN, SUM, CONCAT, UNIQUE
	};

	private final Operator op;
	
	
	public IntegerListAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<?> getSupportedType() {
		return List.class;
	}

	@Override
	public Class<?> getSupportedListType() {
		return Integer.class;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public List<Integer> aggregate(CyTable table, List<CyNode> nodes, CyColumn column) {
		Class<?> listType = column.getListElementType();
		List<Integer> agg = new ArrayList<Integer>();
		List<List<Integer>> aggMed = new ArrayList<>();
		Set<Integer> aggset = new HashSet<Integer>();
		List<Integer> aggregation = null;

		if (op == Operator.NONE)
			return null;
		if (!listType.equals(Integer.class))
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
				Integer value = (Integer) obj;
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
						List<Integer> l = new ArrayList<>();
						l.add(value);
						aggMed.add(index, l);
					}
					break;
				}
				index++;
			}
		}

		if (op == Operator.UNIQUE)
			aggregation = new ArrayList<Integer>(aggset);
		else if (op == Operator.AVG) {
			aggregation = new ArrayList<Integer>();
			for (Integer v : agg) {
				aggregation.add(Math.round((float) v / (float) nodeCount));
			}
		} else if (op == Operator.MEDIAN) {
			aggregation = new ArrayList<Integer>();
			for (List<Integer> valueList : aggMed) {
				Integer[] vArray = new Integer[valueList.size()];
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
