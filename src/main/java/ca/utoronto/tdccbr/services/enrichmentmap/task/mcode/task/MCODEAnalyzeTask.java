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
	
	public final static int FIRST_TIME = 0;
	public final static int RESCORE = 1;
	public final static int REFIND = 2;
	public final static int INTERRUPTION = 3;

	private final MCODEAlgorithm alg;
//	private final MCODEUtil mcodeUtil;
//	private final MCODEResultsManager resultsMgr;
//	private final int mode;
	private final int resultId;

	private boolean cancelled;
	
	private final Supplier<CyNetwork> networkSupplier;
	private CyNetwork network;
	
	private MCODEResult result;
	
//	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);

	/**
	 * Scores and finds clusters in a given network
	 *
	 * @param network The network to cluster
	 * @param mode Tells the task if we need to rescore and/or refind
	 * @param resultId Identifier of the current result set
	 * @param alg reference to the algorithm for this network
	 */
	public MCODEAnalyzeTask(
			Supplier<CyNetwork> networkSupplier
//			int mode,
//			int resultId,
//			MCODEAlgorithm alg,
//			MCODEResultsManager resultsMgr,
//			MCODEUtil mcodeUtil
	) {
		this.networkSupplier = networkSupplier;
//		this.mode = mode;
		this.resultId = 99; //resultId;  // MKTODO hardcode for now
//		this.alg = alg;
		this.alg = new MCODEAlgorithm(null);
//		this.resultsMgr = resultsMgr;
//		this.mcodeUtil = mcodeUtil;
	}

	/**
	 * Run MCODE (Both score and find steps).
	 */
	@Override
	public void run() throws Exception {
//		if (tm == null)
//			throw new IllegalStateException("Task Monitor is not set.");
//
//		tm.setTitle("MCODE Analysis");
//		tm.setProgress(0.0);
		
		network = networkSupplier.get();
		if(network == null)
			return;
		
		try {
			// Run MCODE scoring algorithm - node scores are saved in the alg object
//			alg.setTaskMonitor(tm, network.getSUID());

			// Only (re)score the graph if the scoring parameters have been changed
//			if (mode == RESCORE) {
//				tm.setProgress(0.001);
//				tm.setStatusMessage("Scoring Network (Step 1 of 3)");
				
				alg.scoreGraph(network, resultId);
//
//				if (cancelled)
//					return;

//				logger.info("Network was scored in " + alg.getLastScoreTime() + " ms.");
//			}

//			tm.setProgress(0.001);
//			tm.setStatusMessage("Finding Clusters (Step 2 of 3)");

			var clusters = alg.findClusters(network, resultId);

			if (cancelled || clusters.isEmpty())
				return;
			
//			tm.setProgress(0.5);
//			tm.setStatusMessage("Drawing Results (Step 3 of 3)");
//			result = resultsMgr.createResult(network, alg.getParams().copy(), clusters);
			
			result = new MCODEResult(99, network, alg.getParams().copy(), clusters);
			
			createNetworkAttributes();
//			tm.setProgress(1.0);
		} catch (Exception e) {
			throw new Exception("Error while executing the MCODE analysis", e);
		}
	}

//	@Override
//	public void cancel() {
//		cancelled = true;
//		alg.setCancelled(true);
//		resultsMgr.removeResult(resultId);
//		mcodeUtil.removeNetworkAlgorithm(network.getSUID());
//	}

//	@Override
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public Object getResults(Class type) {
//		if (type == MCODEResult.class)
//			return result;
//		
//		if (type == String.class) {
//			var color = LookAndFeelUtil.getSuccessColor();
//			
//			if (color == null)
//				color = Color.DARK_GRAY;
//			
//			var sb = new StringBuilder();
//			
//			if (result == null) {
//				sb.append("No clusters were found.\n"
//						+ "You can try changing the MCODE parameters or modifying your node selection"
//						+ " if you are using a selection-specific scope."
//				);
//			} else {
//				var clusters = result.getClusters();
//				
//				sb.append(String.format(
//						"<html><body>"
//						+ "<span style='font-family: monospace; color: %1$s;'>Result #" + resultId + ":</span><br /> <br />"
//						+ "<table style='font-family: monospace; color: %1$s;'>"
//						+ "<tr style='font-weight: bold; border-width: 0px 0px 1px 0px; border-style: dotted;'>"
//						+ "<th style='text-align: left;'>Rank</th>"
//						+ "<th style='text-align: left;'>Score</th>"
//						+ "<th style='text-align: left;'>Nodes</th>"
//						+ "<th style='text-align: left;'>Edges</th>"
//						+ "</tr>",
//						("#" + Integer.toHexString(color.getRGB()).substring(2))
//				));
//				
//				for (var c : clusters)
//					sb.append(String.format(
//							"<tr>"
//							+ "<td style='text-align: right;'>%d</td>"
//							+ "<td style='text-align: right;'>%f</td>"
//							+ "<td style='text-align: right;'>%d</td>"
//							+ "<td style='text-align: right;'>%d</td></tr>",
//							c.getRank(),
//							c.getScore(),
//							c.getGraph().getNodeCount(),
//							c.getGraph().getEdgeCount()
//					));
//				
//				sb.append("</table></body></html>");
//			}
//			
//			return sb.toString();
//		}
//		
//		if (type == JSONResult.class) {
//			var gson = new Gson();
//			JSONResult res = () -> { return gson.toJson(result); };
//			
//			return res;
//		}
//		
//		return null;
//	}
	
//	@Override
//	public List<Class<?>> getResultClasses() {
//		return Arrays.asList(MCODEResult.class, String.class, JSONResult.class);
//	}
	
	private void createNetworkAttributes() {
//		tm.setStatusMessage("Creating Node Table columns...");
		MCODEUtil.createMCODEColumns(result);

//		tm.setProgress(0.6);
//		tm.setStatusMessage("Updating Node Table values...");

		int resultId = result.getId();
		var network = result.getNetwork();
		var clusters = result.getClusters();
//		var alg = MCODEUtil.getNetworkAlgorithm(network.getID());

		var scoreCol = columnName(SCORE_ATTR, result);
		var nodeStatusCol = columnName(NODE_STATUS_ATTR, result);
		var clusterCol = columnName(CLUSTERS_ATTR, result);

		for (var n : network.getNodeList()) {
			if (cancelled)
				return;

			var nId = n.getID();
			var nodeRow = network.getRow(n);

			if (cancelled)
				return;

			nodeRow.set(nodeStatusCol, "Unclustered");

			if (cancelled)
				return;

			nodeRow.set(scoreCol, alg.getNodeScore(n.getID(), resultId));

			for (var c : clusters) {
				if (cancelled)
					return;

				if (c.getNodes().contains(nId)) {
					var clusterNameSet = new LinkedHashSet<String>();

//					if (nodeRow.isSet(clusterCol))
					var list = nodeRow.getList(clusterCol, String.class);
					if(list != null)
						clusterNameSet.addAll(list);

					clusterNameSet.add(c.getName());

					if (cancelled)
						return;

					nodeRow.set(clusterCol, new ArrayList<>(clusterNameSet));

					if (cancelled)
						return;

					if (c.getSeedNode() == nId)
						nodeRow.set(nodeStatusCol, "Seed");
					else
						nodeRow.set(nodeStatusCol, "Clustered");
				}
			}
		}
	}
}
