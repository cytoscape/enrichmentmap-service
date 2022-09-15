package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.Collection;
import java.util.List;

import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.network.CyNode;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.cluster.CloudInfo;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.cluster.CloudWordInfo;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model.CloudBuilder;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model.CloudParameters;
import ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model.NetworkParameters;

/**
 * WordCloud and AutoAnnotate are separate apps. In cytoscape desktop AutoAnnotate has to 
 * call WordClound through a command. The command can only return data using common Java
 * types like Map and List, so it has to be copied into a WordInfo object. Here we 
 * keep the basic idea of an Adapter even though it isn't really needed in this context,
 * but just to reduce the amount of code edits.
 * 
 * @author mkucera
 */
public class WordCloudAdapter {

	public WordCloudResults runWordCloud(Collection<CyNode> cluster, CyNetwork network, String labelColumn) {
		
		NetworkParameters networkParams = new NetworkParameters(network);
		CloudBuilder builder = networkParams.getCloudBuilder();
		
		builder.setName("EM temp cloud")
		   .setNodes(cluster)
		   .setAttributes(List.of(labelColumn));
		
		CloudParameters cloudParams = builder.buildFakeCloud();
		CloudInfo cloudInfo = cloudParams.calculateCloud();
		
		var wordInfos = cloudInfo
				.getCloudWordInfoList()
				.stream()
				.map(WordCloudAdapter::adaptWordInfo)
				.toList();
		
		var selectedCounts = cloudInfo.getSelectedCounts();
		return new WordCloudResults(wordInfos, selectedCounts);
	}
	
	
	private static WordInfo adaptWordInfo(CloudWordInfo cloudWord) {
		return new WordInfo(cloudWord.getWord(), cloudWord.getFontSize(), cloudWord.getCluster(), cloudWord.getWordNumber());
	}

}
