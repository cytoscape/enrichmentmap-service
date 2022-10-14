package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.task.Task;

public class ClusterLabelTask implements Task {
	
	private final Supplier<CyNetwork> networkSupplier;
	private final String labelColumn;
	private final String clusterIdColumn;
	private final ClusterBoostedOptions options;
	private Map<String,String> taskResults;
	
	
	
	public ClusterLabelTask(Supplier<CyNetwork> networkSupplier, String labelColumn, String clusterIdColumn, int maxWords) {
		this.labelColumn = labelColumn;
		this.clusterIdColumn = clusterIdColumn;
		this.networkSupplier = networkSupplier;
		this.options = ClusterBoostedOptions.defaults().maxWords(maxWords);
	}

	
	@Override
	public void run() {
		var network = networkSupplier.get();
		
		var labelMaker = new ClusterBoostedLabelMaker(options);
		
		var results = new HashMap<String,String>();
		var clusters = getClusters(network);
		
		clusters.forEach((clusterId, nodes) -> {
			String label = labelMaker.makeLabel(network, nodes, labelColumn);
			results.put(clusterId, label);
		});
		
		this.taskResults = results;
	}
	
	
	private Map<String,List<CyNode>> getClusters(CyNetwork network) {
		var clusters = new HashMap<String,List<CyNode>>();
		
		for(var node : network.getNodeList()) {
			var row = network.getRow(node);
			var clusterIdList = row.getList(clusterIdColumn, String.class);
			if(clusterIdList != null && !clusterIdList.isEmpty()) {
				var clusterId = clusterIdList.get(0);
				clusters.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(node);
			}
		}
		
		return clusters;
	}
	
	
	public Map<String,String> getClusterLabels() {
		return taskResults;
	}
	
	public ClusterBoostedOptions getOptions() {
		return options;
	}

}
