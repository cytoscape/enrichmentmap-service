package ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;


public class NetworkParameters {

	private final CyNetwork network;
	
	//Filter stuff
	private WordFilter filter;
	private WordDelimiters delimiters;
	
	
	
	public NetworkParameters(CyNetwork network) {
		this.network = network;
		this.delimiters = new WordDelimiters(); // default
		this.filter = new WordFilter(); // default
	}
	
	
	public CloudBuilder getCloudBuilder() {
		CloudBuilder builder = new CloudBuilder(this);
		return builder;
	}
	
	
	/**
	 * Note this isn't very well tested, don't call from anywhere except the create cloud command handler.
	 */
	protected CloudParameters createFakeCloud(CloudBuilder builder) {
		CloudParameters cloudParams = new CloudParameters(this, "FakeCloud", -1);
		cloudParams.setOverrideNodes(builder.getNodes());
		cloudParams.setAttributeNames(builder.getAttributeNames());
		cloudParams.setDisplayStyle(builder.getDisplayStyle());
		cloudParams.setMaxWords(builder.getMaxWords());
		cloudParams.setClusterCutoff(builder.getClusterCutoff());
		cloudParams.setNetWeightFactor(builder.getNetWeightFactor());
		cloudParams.setMinWordOccurrence(builder.getMinWordOccurrence());
		return cloudParams;
	}
	
	public CyNetwork getNetwork() {
		return network;
	}
	
	public void setFilter(WordFilter filter) {
		this.filter = filter;
	}
	
	public WordFilter getFilter() {
		return filter;
	}
	
	public void setDelimeters(WordDelimiters delimiters) {
		this.delimiters = delimiters;
	}
	
	public WordDelimiters getDelimeters() {
		return delimiters;
	}
	
	public boolean getIsStemming() {
		return false;
	}
	
}
