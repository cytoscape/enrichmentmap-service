package ca.utoronto.tdccbr.services.enrichmentmap.task.autoannotate;

public class ClusterBoostedOptions {

	
	public static final int DEFAULT_MAX_WORDS = 3;
	public static final int DEFAULT_CLUSTER_BONUS = 8;
	public static final int DEFAULT_MIN_OCCURS = 1;
	
	
	private final int maxWords;	
	private final int clusterBonus;
	private final int minOccurs;
	
	public ClusterBoostedOptions(int maxWords, int clusterBonus, int minOccurs) {
		this.maxWords = maxWords;
		this.clusterBonus = clusterBonus;
		this.minOccurs = minOccurs;
	} 
	
	
	public static ClusterBoostedOptions defaults() {
		return new ClusterBoostedOptions(DEFAULT_MAX_WORDS, DEFAULT_CLUSTER_BONUS, DEFAULT_MIN_OCCURS);
	}
	
	public int getMaxWords() {
		return maxWords;
	}
	
	public int getClusterBonus() {
		return clusterBonus;
	}
	
	public int getMinimumWordOccurrences() {
		return minOccurs;
	}
	
	public ClusterBoostedOptions maxWords(int maxWords) {
		return new ClusterBoostedOptions(maxWords, clusterBonus, minOccurs);
	}
	
	public ClusterBoostedOptions clusterBonus(int clusterBonus) {
		return new ClusterBoostedOptions(maxWords, clusterBonus, minOccurs);
	}
	
	@Override
	public String toString() {
		return "ClusterBoostedOptions [maxWords=" + maxWords + ", clusterBonus=" + clusterBonus + ", minOccurs=" + minOccurs + "]";
	}
	
}
