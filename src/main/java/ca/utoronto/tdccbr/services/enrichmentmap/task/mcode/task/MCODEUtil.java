package ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task;


import java.util.Collection;
import java.util.HashSet;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyEdge;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyTable;
import ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.model.MCODEGraph;
import ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.model.MCODEResult;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 7:00:13 PM
 * * Description: Utilities for MCODE
 */

// TODO refactor: remove circular dependencies
/**
 * Utilities for MCODE
 */
public class MCODEUtil {
	
	public static final String STYLE_TITLE = "MCODE";
	public static final String CLUSTER_IMG_STYLE_TITLE = "MCODE (Cluster Image)";
	
	// Columns
	public static final String NAMESPACE = "MCODE";
	
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String SCORE_ATTR = "Score";
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String NODE_STATUS_ATTR = "Node Status";
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String CLUSTERS_ATTR = "Clusters";
	

	public static MCODEGraph createGraph(CyNetwork net, Collection<CyNode> nodes, boolean includeLoops) {
		var edges = new HashSet<CyEdge>();

		for (CyNode n : nodes) {
			var adjacentEdges = new HashSet<>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (var e : adjacentEdges) {
				if (!includeLoops && e.getSource().getID() == e.getTarget().getID())
					continue;
				
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget()))
					edges.add(e);
			}
		}

		var graph = new MCODEGraph(net, nodes, edges); // TODO remove circular dependency MCODEUtil/MCODEGraph

		return graph;
	}
	
	
	public static void createMCODEColumns(MCODEResult res) {
		createMCODEColumns(res.getNetwork(), res);
	}
	
	/**
	 * @param clusterNet
	 * @param res if null, the column names won't have the result id as suffix
	 */
	public static void createMCODEColumns(CyNetwork net, MCODEResult res) {
		// Create MCODE columns as local ones:
		var table = net.getNodeTable();
		
		createColumn(table, columnName(SCORE_ATTR, res), Double.class, false);
		createColumn(table, columnName(NODE_STATUS_ATTR, res), String.class, false);
		createColumn(table, columnName(CLUSTERS_ATTR, res), String.class, true);
	}
	

	public static void createColumn(CyTable table, String name, Class<?> type, boolean isList) {
		// Create MCODE columns as LOCAL ones
		try {
			if (table.getColumn(name) == null) {
				if (isList)
					table.createListColumn(name, type);
				else
					table.createColumn(name, type);
			}
		} catch (IllegalArgumentException e) {
		}
	}
	

	public static String columnName(String name) {
		return columnName(name, null);
	}
	
	public static String columnName(String name, MCODEResult res) {
		var prefix = NAMESPACE + "::";
		var suffix = res != null ? " (" + res.getId() + ")" : "";
		return prefix + name + suffix;
	}
	
	public static String columnName(String name, int resultId) {
		var prefix = NAMESPACE + "::";
		var suffix = " (" + resultId + ")";
		return prefix + name + suffix;
	}
	
}
