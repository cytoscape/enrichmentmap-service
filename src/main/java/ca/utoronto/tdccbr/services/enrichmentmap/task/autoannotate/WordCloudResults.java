package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

import java.util.List;
import java.util.Map;

public class WordCloudResults {

	private final List<WordInfo> wordInfos;
	private final Map<String,Integer> selectedCounts;
	
	public WordCloudResults(List<WordInfo> wordInfos, Map<String,Integer> selectedCounts) {
		this.wordInfos = wordInfos;
		this.selectedCounts = selectedCounts;
	}

	public List<WordInfo> getWordInfos() {
		return wordInfos;
	}

	public Map<String,Integer> getSelectedCounts() {
		return selectedCounts;
	}
	
	public boolean meetsOccurrenceCount(String word, int minOccurs) {
		if(selectedCounts == null || minOccurs <= 1)
			return true;
		
		Integer count = selectedCounts.get(word);
		if(count == null)
			return true;
		
		return count >= minOccurs;
	}
	
}
