/*
 File: CloudWordInfo.java

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

package ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.cluster;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

/**
 * The CloudWordInfo class defines information pertaining to a particular
 * word in a Cloud.  In particular this class defines information that 
 * relates to how that word will be displayed in the cloud.
 * @author Layla Oesper
 * @version 1.0
 */

public class CloudWordInfo implements Comparable<CloudWordInfo>
{
	private final String word;
	private final int fontSize;
	private final CloudInfo cloudInfo;
	private final Color textColor;
	private final int cluster;
	private final int wordNum;
	
	
	public CloudWordInfo(CloudInfo cloudInfo, String word, int fontSize, Color textColor, int cluster, int wordNum) {
		this.cloudInfo = cloudInfo;
		this.word = word;
		this.fontSize = fontSize;
		this.textColor = textColor;
		this.cluster = cluster;
		this.wordNum = wordNum;
	}
	
	public CloudWordInfo(CloudInfo cloudInfo, String word, int fontSize) {
		this(cloudInfo, word, fontSize, null, 0, 0);
	}

	
	/**
	 * Compares two CloudWordInfo objects based on their fontSize.  Then, based
	 * on cluster number, then based on wordNum, and then alphabetically.
	 * @param CloudWordInfo - object to compare
	 * @return true if 
	 */
	public int compareTo(CloudWordInfo c)
	{
		Integer first = this.getFontSize();
		Integer second = c.getFontSize();
		
		//switch order since we want to sort biggest to smallest
		int result = second.compareTo(first);
		
		if (result == 0)
		{
			first = this.getCluster();
			second = c.getCluster();
			result = first.compareTo(second);
			
			if (result == 0)
			{
				first = this.getWordNumber();
				second = c.getWordNumber();
				result = first.compareTo(second);
				
				if (result == 0)
				{
					String firstString = this.getWord();
					String secondString = c.getWord();
					result = firstString.compareTo(secondString);
				}//end string compare
			}//end word number compare
		}//end cluster compare
		
		return result;
	}
	
	/**
	 * Returns a JLabel that can be used to display this word in a cloud.
	 * @return JLabel - for display in Cloud.
	 */
	public JLabel createCloudLabel()
	{
		JLabel label = new JLabel(word);
		label.setFont(new Font("sansserif",Font.BOLD, fontSize));
		label.setForeground(textColor);
		return label;
	}
	
	@Override
	public String toString() {
		return "CloudWordInfo [word=" + word + ", fontSize=" + fontSize + ", cluster=" + cluster + ", wordNum=" + wordNum + "]";
	}

	
	//Getters and Setters
	public String getWord()
	{
		return word;
	}
	
	public int getFontSize()
	{
		return fontSize;
	}
	
	public CloudInfo getCloudInfo()
	{
		return cloudInfo;
	}
	
	public Color getTextColor()
	{
		return textColor;
	}
	
	public int getCluster()
	{
		return cluster;
	}
	
	public int getWordNumber()
	{
		return wordNum;
	}
	
}
