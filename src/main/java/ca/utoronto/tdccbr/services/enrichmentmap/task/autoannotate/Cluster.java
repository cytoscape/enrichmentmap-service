package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;

public class Cluster {

	private final Set<CyNode> nodes;
	
	public Cluster(Set<CyNode> nodes) {
		this.nodes = nodes;
	}

	public Set<CyNode> getNodes() {
		return nodes;
	}
	
}
