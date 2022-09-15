/*
 File: CloudParameters.java

 Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
 
 Code written by: Layla Oesper
 Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
 
 This library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.cluster.CloudDisplayStyles;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.cluster.CloudInfo;


/**
 * The CloudParameters class defines all of the variables that are
 * needed to create a word Cloud for a particular network, attribute, 
 * and set of selected nodes.
 * @author Layla Oesper
 * @version 1.0
 *
 */

public class CloudParameters implements Comparable<CloudParameters>
{
	//Default Values for User Input
	public static final double DEFAULT_NET_WEIGHT = 0.5; // this can be overridden in wordcloud.props
	public static final String DEFAULT_ATT_NAME = CyNetwork.NAME;
	public static final int    DEFAULT_MAX_WORDS = 250;
	public static final double DEFAULT_CLUSTER_CUTOFF = 1.0;
	public static final int    DEFAULT_MIN_OCCURRENCE = 1;
	
	private final NetworkParameters networkParams; //parent network
	private CloudInfo cloudWordInfoBuilder;
	private boolean calculated = false;
	
	private String cloudName;
	private List<String> attributeNames;
	private CloudDisplayStyles displayStyle;
	
	private int cloudNum; //Used to order the clouds for each network
	private int maxWords = DEFAULT_MAX_WORDS;
	private double clusterCutoff =  DEFAULT_CLUSTER_CUTOFF;
	private double netWeightFactor = DEFAULT_NET_WEIGHT;
	private int minWordOccurrence = DEFAULT_MIN_OCCURRENCE;
	
	/** Allows to explicitiy set the nodes instead of using a table column */
	private Set<CyNode> overrideNodes = null;
	
	/**
	 * Default constructor to create a fresh instance
	 */
	protected CloudParameters(NetworkParameters networkParams, String cloudName, int cloudNum) {
		if(cloudName == null)
			throw new NullPointerException();
		this.networkParams = networkParams;
		this.displayStyle = CloudDisplayStyles.getDefault();
		this.cloudNum = cloudNum;
		this.cloudName = cloudName;
	}
	
	
	public void invalidate() {
		calculated = false;
		cloudWordInfoBuilder = null;
	}
	
	
	/**
	 * Returns the object that is responsible for calculating the cloud.
	 * Warning this method has the potential to be long running.
	 * Warning this method is synchronized, so only one thread may be
	 * calculated the could at one time. This is done because 
	 * the CloudParameters object is mutable.
	 */
	public synchronized CloudInfo calculateCloud() {
		if(cloudWordInfoBuilder == null) {
			cloudWordInfoBuilder = new CloudInfo(this);
			cloudWordInfoBuilder.calculateFontSizes();
			calculated = true;
		}
		return cloudWordInfoBuilder;
	}
	
	/**
	 * Returns true if the cloud has already been calculated.
	 */
	public boolean isAlreadyCalculated() {
		return calculated;
	}
	
	
	/**
	 * Compares two CloudParameters objects based on the order in which they
	 * were created.
	 * @param CloudParameters object to compare this object to
	 * @return
	 */
	public int compareTo(CloudParameters other) 
	{	
		return cloudNum - other.cloudNum;
	}
	
	
	//Getters and Setters
	public String getCloudName()
	{
		return cloudName;
	}
	
	
	public List<String> getAttributeNames()
	{
		return attributeNames;
	}
	
	public void setAttributeNames(List<String> names)
	{
		//Check if we need to reset flags
		boolean changed = false;
		if (attributeNames == null || names.size() != attributeNames.size())
			changed = true;
		else
		{
			for (int i = 0; i < names.size(); i++)
			{
				String curAttribute = names.get(i);
				
				if (!attributeNames.contains(curAttribute))
				{
					changed = true;
					continue;
				}
			}
		}
		
		//Set flags
		if (changed)
		{
			invalidate();
		}
		
		//Set to new value
		attributeNames = names;
	}
	
	public void addAttributeName(String name)
	{
		if (attributeNames == null) {
			attributeNames = new ArrayList<String>();
		}
		
		if (!attributeNames.contains(name))
		{
			attributeNames.add(name);
			invalidate();
		}
	}
	
	public void removeAttribtueName(String name) {
		if(attributeNames != null) {
			boolean removed = attributeNames.remove(name);
			if(removed) {
				invalidate();
			}
		}
	}
	

	
	public NetworkParameters getNetworkParams()
	{
		return networkParams;
	}
	
	public Set<CyNode> getSelectedNodes()
	{
		return overrideNodes;
	}
	
	void setOverrideNodes(Collection<CyNode> nodes) {
		this.overrideNodes = new HashSet<>(nodes);
	}

	public int getNetworkNumNodes()
	{
		if (networkParams == null) {
			return 0;
		}
		CyNetwork network = networkParams.getNetwork();
		if (network == null) {
			return 0;
		}
		return network.getNodeCount();
	}

	public double getMinRatio()
	{
		return cloudWordInfoBuilder.getMinRatio();
	}
	
	public double getMaxRatio()
	{
		return cloudWordInfoBuilder.getMaxRatio();
	}
	
	public double getNetWeightFactor()
	{
		return netWeightFactor;
	}
	
	public void setNetWeightFactor(double val)
	{
		netWeightFactor = val;
	}
	
	public double getClusterCutoff()
	{
		return clusterCutoff;
	}
	
	public void setClusterCutoff(double val)
	{
		clusterCutoff = val;
	}
	
	public int getMaxWords()
	{
		return maxWords;
	}
	
	public void setMaxWords(int val)
	{
		maxWords = val;
	}
	
	public int getMinWordOccurrence()
	{
		return minWordOccurrence;
	}
	
	public void setMinWordOccurrence(int val) 
	{
		minWordOccurrence = val;
	}
	
	public int getCloudNum()
	{
		return cloudNum;
	}
	
	public CloudDisplayStyles getDisplayStyle()
	{
		return displayStyle;
	}
	
	public void setDisplayStyle(CloudDisplayStyles style)
	{
		displayStyle = style;
	}
}
