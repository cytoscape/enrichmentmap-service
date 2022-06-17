package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDataSet implements Comparable<AbstractDataSet> {

	private final String name;
	private final Set<String> nodeIds = new HashSet<>();
	private final Set<String> edgeIds = new HashSet<>();
	
	/** EnrichmentMap only creates nodes for these genes. */
	private SetOfGeneSets geneSetsOfInterest = new SetOfGeneSets();
	
	//TODO: Can a dataset be associated to multiple Enrichment maps?
	/** A Dataset is always associated with an Enrichment Map. */
	private transient EnrichmentMap map;
		
	private static final Collator collator = Collator.getInstance();
	private final Object lock = new Object();
	
	protected AbstractDataSet(EnrichmentMap map, String name) {
		this.map = map;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * @noreference
	 * This method is only meant to be called by the ModelSerializer.
	 */
	public void setParent(EnrichmentMap map) {
		this.map = map;
	}
	
	public EnrichmentMap getMap() {
		return map;
	}

	public boolean containsAnyNode(Collection<CyNode> nodes) {
		for (var n : nodes) {
			if (nodeIds.contains(n.getId()))
				return true;
		}
		return false;
	}

	public boolean containsAnyEdge(Collection<CyEdge> edges) {
		for (var e : edges) {
			if (edgeIds.contains(e.getId()))
				return true;
		}
		return false;
	}

	public Set<String> getNodeIds() {
		synchronized (lock) {
			return Collections.unmodifiableSet(nodeIds);
		}
	}

	public void setNodeIds(Set<String> newValue) {
		synchronized (lock) {
			nodeIds.clear();
			nodeIds.addAll(newValue);
		}
	}

	public void addNodeId(String id) {
		synchronized (lock) {
			nodeIds.add(id);
		}
	}

	public void clearNodeIds() {
		synchronized (lock) {
			nodeIds.clear();
		}
	}

	public Set<String> getEdgeIds() {
		synchronized (lock) {
			return Collections.unmodifiableSet(edgeIds);
		}
	}

	public void setEdgeIds(Set<String> newValue) {
		synchronized (lock) {
			edgeIds.clear();
			edgeIds.addAll(newValue);
		}
	}

	public void addEdgeId(String id) {
		synchronized (lock) {
			edgeIds.add(id);
		}
	}

	public void clearEdgeIds() {
		synchronized (lock) {
			edgeIds.clear();
		}
	}

	public SetOfGeneSets getGeneSetsOfInterest() {
		return geneSetsOfInterest;
	}

	public void setGeneSetsOfInterest(SetOfGeneSets geneSetsOfInterest) {
		this.geneSetsOfInterest = geneSetsOfInterest;
	}
		
	@Override
	public int compareTo(AbstractDataSet other) {
		return collator.compare(getName(), other.getName());
	}
}
