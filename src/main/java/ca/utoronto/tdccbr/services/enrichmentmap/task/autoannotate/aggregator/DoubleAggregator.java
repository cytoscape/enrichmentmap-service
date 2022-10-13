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
import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyColumn;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyIdentifiable;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;

public class DoubleAggregator implements Aggregator<Double> {
	
	public static enum Operator {
		NONE, AVG, MIN, MAX, MEDIAN, SUM 
	};

	private final Operator op;
	
	public DoubleAggregator(Operator op) {
		this.op = op;
	}

	@Override
	public Class<Double> getSupportedType() {
		return Double.class;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public Double aggregate(CyTable table, List<? extends CyIdentifiable> eles, CyColumn column) {
		double aggregation = 0.0;
		int count = 0;
		List<Double> valueList = null;

		if (op == Operator.NONE)
			return null;

		// Initialization
		switch (op) {
		case MAX:
			aggregation = Double.MIN_VALUE;
			break;
		case MIN:
			aggregation = Double.MAX_VALUE;
			break;
		case MEDIAN:
			valueList = new ArrayList<Double>();
			break;
		}

		// Loop processing
		for (var ele : eles) {
			Double v = table.getRow(ele.getID()).get(column.getName(), Double.class);
			if (v == null)
				continue;
			double value = v.doubleValue();
			count++;
			switch (op) {
			case MAX:
				if (aggregation < value)
					aggregation = value;
				break;
			case MIN:
				if (aggregation > value)
					aggregation = value;
				break;
			case SUM:
				aggregation += value;
				break;
			case AVG:
				aggregation += value;
				break;
			case MEDIAN:
				valueList.add(value);
				break;
			}
		}

		// Post processing
		if (op == Operator.MEDIAN) {
			Double[] vArray = new Double[valueList.size()];
			vArray = valueList.toArray(vArray);
			Arrays.sort(vArray);
			if (vArray.length % 2 == 1)
				aggregation = vArray[(vArray.length - 1) / 2];
			else
				aggregation = (vArray[(vArray.length / 2) - 1] + vArray[(vArray.length / 2)]) / 2;

		} else if (op == Operator.AVG) {
			aggregation = aggregation / (double) count;
		}

		return aggregation;
	}
}
