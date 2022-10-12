package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;

public class Cluster {

	private final Set<CyNode> nodes;
	private final String label;
	
	public Cluster(Set<CyNode> nodes, String label) {
		this.nodes = nodes;
		this.label = label;
	}
	
	public Cluster(Set<CyNode> nodes) {
		this(nodes, null);
	}

	public Set<CyNode> getNodes() {
		return nodes;
	}

	public String getLabel() {
		return label;
	}
	
}
