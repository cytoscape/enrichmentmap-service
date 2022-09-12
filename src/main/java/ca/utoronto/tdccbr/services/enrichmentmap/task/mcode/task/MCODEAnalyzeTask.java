package ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task;

import static ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task.MCODEUtil.CLUSTERS_ATTR;
import static ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task.MCODEUtil.NODE_STATUS_ATTR;
import static ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task.MCODEUtil.SCORE_ATTR;
import static ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.task.MCODEUtil.columnName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

//import org.cytoscape.util.swing.LookAndFeelUtil;
//import org.cytoscape.work.AbstractTask;
//import org.cytoscape.work.ObservableTask;
//import org.cytoscape.work.TaskMonitor;
//import org.cytoscape.work.json.JSONResult;

//import com.google.gson.Gson;

//import ca.utoronto.tdccbr.mcode.internal.action.AnalysisAction;
//import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.task.Task;
import ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.services.enrichmentmap.task.mcode.model.MCODEResult;

/**
 * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
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
 * * User: GaryBader
 * * Date: Jan 25, 2005
 * * Time: 8:41:53 PM
 * * Description: MCODE Score network and find cluster task
 */

/**
 * MCODE Score network and find cluster task.
 */
public class MCODEAnalyzeTask implements Task {
	
	public static final int DEF_RESULT_ID = 1;
	
	public final static int FIRST_TIME = 0;
	public final static int RESCORE = 1;
	public final static int REFIND = 2;
	public final static int INTERRUPTION = 3;

	private final MCODEAlgorithm alg;
	private final int resultId = DEF_RESULT_ID; // Hardcoded for now

	private final Supplier<CyNetwork> networkSupplier;
	private CyNetwork network;
	
	private MCODEResult result;
	
	/**
	 * Scores and finds clusters in a given network
	 *
	 * @param network The network to cluster
	 * @param mode Tells the task if we need to rescore and/or refind
	 * @param resultId Identifier of the current result set
	 * @param alg reference to the algorithm for this network
	 */
	public MCODEAnalyzeTask(Supplier<CyNetwork> networkSupplier) {
		this.networkSupplier = networkSupplier;
		this.alg = new MCODEAlgorithm(null);
	}

	/**
	 * Run MCODE (Both score and find steps).
	 */
	@Override
	public void run() throws Exception {
		network = networkSupplier.get();
		if(network == null)
			return;
		
		try {
				
			alg.scoreGraph(network, resultId);

			var clusters = alg.findClusters(network, resultId);

			if (clusters.isEmpty())
				return;
			
			result = new MCODEResult(resultId, network, alg.getParams().copy(), clusters);
			
			createNetworkAttributes();
		} catch (Exception e) {
			throw new Exception("Error while executing the MCODE analysis", e);
		}
	}

	
	private void createNetworkAttributes() {
		MCODEUtil.createMCODEColumns(result);

		int resultId = result.getId();
		var network = result.getNetwork();
		var clusters = result.getClusters();

		var scoreCol = columnName(SCORE_ATTR, result);
		var nodeStatusCol = columnName(NODE_STATUS_ATTR, result);
		var clusterCol = columnName(CLUSTERS_ATTR, result);

		for (var n : network.getNodeList()) {

			var nId = n.getID();
			var nodeRow = network.getRow(n);

			nodeRow.set(nodeStatusCol, "Unclustered");

			nodeRow.set(scoreCol, alg.getNodeScore(n.getID(), resultId));

			for (var c : clusters) {
				if (c.getNodes().contains(nId)) {
					var clusterNameSet = new LinkedHashSet<String>();

					var list = nodeRow.getList(clusterCol, String.class);
					if(list != null)
						clusterNameSet.addAll(list);

					clusterNameSet.add(c.getName());

					nodeRow.set(clusterCol, new ArrayList<>(clusterNameSet));

					if (c.getSeedNode() == nId)
						nodeRow.set(nodeStatusCol, "Seed");
					else
						nodeRow.set(nodeStatusCol, "Clustered");
				}
			}
		}
	}
}
