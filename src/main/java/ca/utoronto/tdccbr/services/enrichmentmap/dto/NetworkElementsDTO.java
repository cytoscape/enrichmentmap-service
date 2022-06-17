package ca.utoronto.tdccbr.services.enrichmentmap.dto;

import java.util.ArrayList;
import java.util.List;

public class NetworkElementsDTO {

	private List<NodeDTO> nodes;
	private List<EdgeDTO> edges;

	public List<NodeDTO> getNodes() {
		if (nodes == null)
			nodes = new ArrayList<>();
		
		return nodes;
	}

	public void setNodes(List<NodeDTO> nodes) {
		this.nodes = nodes;
	}

	public List<EdgeDTO> getEdges() {
		if (edges == null)
			edges = new ArrayList<>();
		
		return edges;
	}

	public void setEdges(List<EdgeDTO> edges) {
		this.edges = edges;
	}

	@Override
	public String toString() {
		return "NetworkElementsDTO [nodes=" + (nodes != null ? nodes.size() : 0) + ", edges=" + (edges != null ? edges.size() : 0) + "]";
	}
}
