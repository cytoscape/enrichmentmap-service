package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.EMCreationParametersDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.FGSEAEnrichmentResultDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet.Method;

/***
 * An Enrichment Map object contains the minimal information needed to build an enrichment map.
 */
public class EnrichmentMap {

	/** Parameters used to create this map */
	private final EMCreationParametersDTO params;

	private final Map<String, EMDataSet> dataSets = new HashMap<>();

	/**
	 * In order to minimize memory usage (and session file size) we will store expressions
	 * here and have the EMDataSets keep a key to this map. That way datasets can share
	 * expression matrices.
	 */
	private final Map<String, GeneExpressionMatrix> expressions = new HashMap<>();
	private final Map<String, SetOfGeneSets> geneSets = new HashMap<>();
	
	/** The set of genes defined in the Enrichment map. */
	private final BiMap<Integer, String> genes = HashBiMap.create();

	/** Post analysis signature genesets associated with this map.*/
	private final Map<String, EMSignatureDataSet> signatureDataSets = new HashMap<>();
	
	private int NumberOfGenes;
	private boolean isLegacy;
	private boolean isDistinctExpressionSets;
	private boolean isCommonExpressionValues;
	
	private Color compoundEdgeColor;

	private final Object lock = new Object();
	
	/**
	 * Used by the JSON deserializer only. Don't remove this constructor!
	 */
	@SuppressWarnings("unused")
	private EnrichmentMap() {
		this(null);
	}
	
	/**
	 * Class Constructor Given - EnrichmentnMapParameters create a new
	 * enrichment map. The parameters contain all cut-offs and file names for the analysis
	 */
	public EnrichmentMap(EMCreationParametersDTO params) {
		this.params = params != null ? params : new EMCreationParametersDTO();
	}

	public EMDataSet createDataSet(String name, List<FGSEAEnrichmentResultDTO> fgseaRes) {
		if (dataSets.containsKey(name))
			throw new IllegalArgumentException("DataSet with name " + name + " already exists in this enrichment map");

		var ds = new EMDataSet(this, name, fgseaRes);
		dataSets.put(name, ds);
		
		return ds;
	}
	
	public void putExpressionMatrix(String key, GeneExpressionMatrix matrix) {
		expressions.put(key, matrix);
	}
	
	public GeneExpressionMatrix getExpressionMatrix(String key) {
		return expressions.get(key);
	}
	
	public GeneExpressionMatrix removeExpressionMatrix(String key) {
		return expressions.remove(key);
	}
	
	public Collection<String> getExpressionMatrixKeys() {
		return Collections.unmodifiableCollection(expressions.keySet());
	}
	
	public void putGeneSets(String key, SetOfGeneSets matrix) {
		geneSets.put(key, matrix);
	}
	
	public SetOfGeneSets getGeneSets(String key) {
		return geneSets.get(key);
	}
	
	public SetOfGeneSets removeGeneSets(String key) {
		return geneSets.remove(key);
	}
	
	public Collection<String> getGeneSetsKeys() {
		return Collections.unmodifiableCollection(geneSets.keySet());
	}
	
	public boolean hasClassData() {
		for (var ds : dataSets.values()) {
			var classes = ds.getEnrichments().getPhenotypes();
			
			if (classes != null && classes.length > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean containsGene(String gene) {
		return genes.containsValue(gene);
	}

	public String getGeneFromHashKey(Integer hash) {
		return genes.get(hash);
	}
	
	public Integer getHashFromGene(String gene) {
		// MKTODO should I toUpperCase?
		return genes.inverse().get(gene);
	}
	
	/**
	 * Returns ALL of the genes that have ever been loaded. Warning: this is probably not what you
	 * want because you probably want a set of genes that has been filtered somehow.
	 */
	public Set<String> getAllGenes() {
		return Collections.unmodifiableSet(genes.values());
	}
	
	public Integer addGene(String gene) {
		if (gene == null || gene.isEmpty())
			return null;

		gene = gene.toUpperCase();

		var geneToHash = genes.inverse();

		Integer hash = geneToHash.get(gene);
		
		if (hash != null)
			return hash;

		Integer newHash = ++NumberOfGenes;
		genes.put(newHash, gene);
		
		return newHash;
	}

	@Deprecated // this is here to support legacy session loading
	public void addGene(String gene, int id) {
		genes.put(id, gene);
		
		if (id > NumberOfGenes)
			NumberOfGenes = id;
	}

	public int getNumberOfGenes() {
		return NumberOfGenes;
	}

	public void setNumberOfGenes(int numberOfGenes) {
		NumberOfGenes = numberOfGenes;
	}

	/**
	 * Filter all the genesets by the dataset genes If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes
	 */
	@Deprecated
	public void filterGenesets() {
		for (var ds : dataSets.values()) {
			// only filter the genesets if dataset genes are not null or empty
			var expressionGenes = ds.getExpressionGenes();

			if (expressionGenes != null && !expressionGenes.isEmpty())
				ds.getSetOfGeneSets().filterGeneSets(expressionGenes);
		}
	}

	/*
	 * Return a hash of all the genesets in the set of genesets regardless of which dataset it comes from.
	 */
	@Deprecated
	public Map<String, GeneSet> getAllGeneSets() {
		// Go through each dataset and get the genesets from each
		var allGeneSets = new HashMap<String, GeneSet>();
		
		synchronized (lock) {
			// If a GeneSet appears in more than one DataSet, then its totally arbitrary which version of it gets picked
			// If a GeneSet appears in an enrichment file it will override the one with the same name in the global GMT file
			for (var ds : dataSets.values()) {
				allGeneSets.putAll(ds.getSetOfGeneSets().getGeneSets());
			}
			
			if (signatureDataSets != null) {
				for (var sds : signatureDataSets.values())
					allGeneSets.putAll(sds.getGeneSetsOfInterest().getGeneSets());
			}
		}
		
		return allGeneSets;
	}

	/*
	 * Return a hash of all the genesets but not inlcuding the signature genesets.
	 */
	@Deprecated
	public Map<String, GeneSet> getEnrichmentGenesets() {
		//go through each dataset and get the genesets from each
		var allGeneSets = new HashMap<String, GeneSet>();
		
		for (var ds : dataSets.values()) {
			var geneSets = ds.getSetOfGeneSets().getGeneSets();
			allGeneSets.putAll(geneSets);
		}
		
		return allGeneSets;
	}
	
	public Map<String, Set<Integer>> unionAllGeneSetsOfInterest() {
		return unionGeneSetsOfInterest(x -> true);
	}
	
	public Map<String, Set<Integer>> unionGeneSetsOfInterest(Collection<? extends AbstractDataSet> dataSets) {
		return unionGeneSetsOfInterest(dataSets::contains);
	}
	
	private Map<String, Set<Integer>> unionGeneSetsOfInterest(Predicate<EMDataSet> filter) {
		var allGeneSets = new HashMap<String, Set<Integer>>();

		for (var ds : getDataSetList()) {
			if (filter.test(ds)) {
				var geneSets = ds.getGeneSetsOfInterest().getGeneSets();

				geneSets.forEach((name, gs) -> {
					allGeneSets.computeIfAbsent(name, k -> new HashSet<>()).addAll(gs.getGenes());
				});
			}
		}

		return allGeneSets;
	}
	
	/**
	 * Returns a set of all genes in the map that are of interest and not from a signature data set.
	 */
	public Set<Integer> getAllEnrichmentGenes() {
		var genes = new HashSet<Integer>();

		for (var ds : getDataSetList()) {
			var geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			
			for (GeneSet geneSet : geneSets.values()) {
				genes.addAll(geneSet.getGenes());
			}
		}
		return genes;
	}
	
	// MKTODO write a JUnit
	public Set<String> getAllGeneSetOfInterestNames() {
		var names = new HashSet<String>();
		
		for (var ds : getDataSetList()) {
			var geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			names.addAll(geneSets.keySet());
		}
		
		return names;
	}
	
	public GeneSet getGeneSet(String genesetName) {
		for (var ds : dataSets.values()) {
			var gs = ds.getGeneSetsOfInterest().getGeneSets().get(genesetName);

			if (gs != null)
				return gs;
		}

		return null;
	}

	public Map<String, EMDataSet> getDataSets() {
		// this must return a new HashMap or client code might break
		return new HashMap<>(dataSets);
	}

	/**
	 * Returns all the DataSets in a predictable order.
	 */
	public List<EMDataSet> getDataSetList() {
		var list = new ArrayList<EMDataSet>(dataSets.values());
		list.sort(Comparator.naturalOrder());
		
		return list;
	}

	public void setDataSets(Map<String, EMDataSet> dataSets) {
		this.dataSets.clear();
		
		if (dataSets != null && !dataSets.isEmpty())
			this.dataSets.putAll(dataSets);
	}

	/**
	 * Returns the total number of data sets, excluding Signature Data Sets.
	 */
	public int getDataSetCount() {
		return dataSets.size();
	}
	
	public EMDataSet getDataSet(String dataSetName) {
		return dataSets.get(dataSetName);
	}
	
	/**
	 * Returns all the DataSet names in a predictable order.
	 */
	public List<String> getDataSetNames() {
		return getDataSetList().stream().map(EMDataSet::getName).collect(Collectors.toList());
	}
	
	public EMCreationParametersDTO getParams() {
		return params;
	}

	public static Set<UUID> getNodesUnion(Collection<? extends AbstractDataSet> dataSets) {
		return getUnion(dataSets, AbstractDataSet::getNodeIds);
	}
	
	public static Set<UUID> getNodesIntersection(Collection<? extends AbstractDataSet> dataSets) { 
		return getIntersection(dataSets, AbstractDataSet::getNodeIds);
	}
			
	/**
	 * Returns the IDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained in at least one of the given DataSets.
	 * 
	 * Note, this will only return distinct edges, not compound edges.
	 */
	public static Set<UUID> getEdgesUnion(Collection<? extends AbstractDataSet> dataSets) {
		return getUnion(dataSets, AbstractDataSet::getEdgeIds);
	}
	
	/**
	 * Returns the IDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained all of the given DataSets.
	 * 
	 * Note, this will only return distinct edges, not compound edges.
	 */
	public static Set<UUID> getEdgesIntersection(Collection<? extends AbstractDataSet> dataSets) {
		return getIntersection(dataSets, AbstractDataSet::getEdgeIds);
	}
	
	private static Set<UUID> getUnion(
			Collection<? extends AbstractDataSet> dataSets,
			Function<AbstractDataSet,
			Set<UUID>> idSupplier
	) {
		if (dataSets.isEmpty())
			return Collections.emptySet();

		var ids = new HashSet<UUID>();

		for (var ds : dataSets) {
			ids.addAll(idSupplier.apply(ds));
		}

		return ids;
	}
	
	private static Set<UUID> getIntersection(
			Collection<? extends AbstractDataSet> dataSets,
			Function<AbstractDataSet,
			Set<UUID>> idSupplier
	) {
		if (dataSets.isEmpty())
			return Collections.emptySet();

		var iter = dataSets.iterator();
		var first = iter.next();
		var ids = new HashSet<>(idSupplier.apply(first));

		while (iter.hasNext()) {
			var ds = iter.next();
			ids.retainAll(idSupplier.apply(ds));
		}

		return ids;
	}
	
	
	public Set<String> getAllRankNames() {
		var allRankNames = new HashSet<String>();
		
		//go through each Dataset
		for (var ds : dataSets.values()) {
			// there could be duplicate ranking names for two different datasets. Add the dataset to the ranks name
			var allNames = ds.getAllRanksNames();

			for (var name : allNames)
				allRankNames.add(name + "-" + ds.getName());
		}
		
		return allRankNames;
	}

	public Map<String, Ranking> getAllRanks() {
		var allranks = new HashMap<String, Ranking>();
		
		for (var ds : dataSets.values())
			allranks.putAll(ds.getRanks());
		
		return allranks;
	}
	
	/**
	 * Returns true if every data set contains exactly one Ranks object.
	 */
	public boolean isSingleRanksPerDataset() {
		for (var ds : dataSets.values()) {
			if (ds.getRanks().size() != 1)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the total number of expressions in the map.
	 */
	public int totalExpressionCount() {
		int count = 0;
		
		for (var ds : dataSets.values()) {
			count += ds.getExpressionSets().getNumConditions() - 2;
		}
		
		return count;
	}

	public Ranking getRanksByName(String ranksName) {
		// break the ranks file up by "-"
		// check to see if the rank file is dataset specific
		// needed for encoding the same ranking file name from two different dataset in the interface
		var ds = "";
		var rank = "";
		
		if (ranksName.split("-").length == 2) {
			ds = ranksName.split("-")[1];
			rank = ranksName.split("-")[0];
		}

		for (var k = dataSets.keySet().iterator(); k.hasNext();) {
			var nextDs = k.next();

			if (!ds.equalsIgnoreCase("") && !rank.equalsIgnoreCase("")) {
				// check that this is the right dataset
				if (ds.equalsIgnoreCase(nextDs) && (dataSets.get(nextDs)).getAllRanksNames().contains(rank))
					return dataSets.get(nextDs).getRanksByName(rank);
			} else if ((dataSets.get(nextDs)).getAllRanksNames().contains(ranksName)) {
				return dataSets.get(nextDs).getRanksByName(ranksName);
			}
		}
		
		return null;
	}

	public EMSignatureDataSet getSignatureDataSet(String name) {
		return signatureDataSets.get(name);
	}
	
	public void setSignatureDataSets(Collection<EMSignatureDataSet> newValue) {
		synchronized (lock) {
			signatureDataSets.clear();
			
			if (newValue != null && !newValue.isEmpty()) {
				for (var sigDataSet: newValue)
					addSignatureDataSet(sigDataSet);
			}
		}
	}

	public Map<String, EMSignatureDataSet> getSignatureDataSets() {
		return new HashMap<>(signatureDataSets);
	}
	
	public boolean hasSignatureDataSets() {
		return !signatureDataSets.isEmpty();
	}
	
	public List<EMSignatureDataSet> getSignatureSetList() {
		var list = new ArrayList<>(signatureDataSets.values());
		list.sort(Comparator.naturalOrder());
		
		return list;
	}
	
	public void addSignatureDataSet(EMSignatureDataSet sigDataSet) {
		synchronized (lock) {
			signatureDataSets.put(sigDataSet.getName(), sigDataSet);
		}
	}
	
	public void removeSignatureDataSet(EMSignatureDataSet sigDataSet) {
		synchronized (lock) {
			signatureDataSets.remove(sigDataSet.getName());
		}
	}

	public void setDistinctExpressionSets(boolean d) {
		this.isDistinctExpressionSets = d;
	}
	
	public boolean isDistinctExpressionSets() {
		return isDistinctExpressionSets;
	}
	
	public void setCommonExpressionValues(boolean b) {
		this.isCommonExpressionValues = b;
	}
	
	public boolean isCommonExpressionValues() {
		return isCommonExpressionValues;
	}
	
	public void setLegacy(boolean legacy) {
		this.isLegacy = legacy;
	}
	
	public Color getCompoundEdgeColor() {
		return compoundEdgeColor;
	}
	
	public void setCompoundEdgeColor(Color compoundEdgeColor) {
		this.compoundEdgeColor = compoundEdgeColor;
	}
	
	/**
	 * Files loaded by LegacySessionLoader should set this flag to true
	 */
	public boolean isLegacy() {
		return isLegacy;
	}
	
	public boolean isTwoPhenotypeGeneric() {
		return dataSets.values().stream().allMatch(EMDataSet::getIsTwoPheotypeGeneric);
	}
	
	public boolean hasNonGSEADataSet() {
		return dataSets.values().stream().anyMatch(ds -> ds.getMethod() != Method.GSEA);
	}
}
