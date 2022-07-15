package ca.utoronto.tdccbr.services.enrichmentmap.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GeneSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.SetOfGeneSets;

/**
 * This class parses a GMT (gene set) file and creates a set of genesets
 */
public class GMTFileReaderTask implements Task {
	
	public static final String DATASET_NAME_1 = "/Human_GOBP_AllPathways_no_GO_iea_June_01_2022_symbol.gmt";
	

	private final EnrichmentMap map;
	private final SetOfGeneSets setOfGeneSets;
	private final Supplier<String> fileNameSupplier;
	private final Consumer<SetOfGeneSets> geneSetConsumer;

	private Pattern baderlabPattern;

	public GMTFileReaderTask(EMDataSet dataset, String fileName) {
		this.map = dataset.getMap();
		this.fileNameSupplier = () -> fileName;
		this.setOfGeneSets = dataset.getSetOfGeneSets();
		this.geneSetConsumer = null;
	}
	
	
	private Pattern getBaderlabPattern() {
		if(baderlabPattern == null) {
			baderlabPattern = Pattern.compile("(.+)%(.+)%(.+)");
		}
		return baderlabPattern;
	}
	
	@Override
	public void run() throws Exception {
		parse();
	}
	
	public void parse() throws IOException {
		String fileName = fileNameSupplier.get();
		boolean baderlab = true; // map.getParams().isParseBaderlabGeneSets();
		
		try (var inputStream = GMTFileReaderTask.class.getResourceAsStream(fileName);
			 var inputReader = new InputStreamReader(inputStream);
			 var reader = new BufferedReader(inputReader)) {
		
			for (String line; (line = reader.readLine()) != null;) {
				GeneSet gs;
				if(baderlab)
					gs = readBaderlabGeneSet(map, line);
				else 
					gs = readGeneSet(map, line);
				
				
				if (gs != null && setOfGeneSets != null) {
					Map<String, GeneSet> genesets = setOfGeneSets.getGeneSets();
					genesets.put(gs.getName(), gs);
				}
				if(geneSetConsumer != null) {
					geneSetConsumer.accept(setOfGeneSets);
				}
			}
		}
	}

	private static GeneSet readGeneSet(EnrichmentMap map, String line) {
		String[] tokens = line.split("\t");
		//only go through the lines that have at least a gene set name and description.
		if(tokens.length >= 2) {
			// set of genes keys
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			String name = tokens[0].toUpperCase().trim();
			String description = tokens[1].trim();
			
			for(int i = 2; i < tokens.length; i++) {
				Integer hash = map.addGene(tokens[i]);
				if(hash != null)
					builder.add(hash);
			}
			return new GeneSet(name, description, builder.build());
		}
		return null;
	}
	
	
	private GeneSet readBaderlabGeneSet(EnrichmentMap map, String line) {
		String[] tokens = line.split("\t");
		if(tokens.length >= 2) {
			final String name = tokens[0].toUpperCase().trim();
			final String description = tokens[1].trim();
			
			Pattern pattern = getBaderlabPattern();
			Matcher m = pattern.matcher(name);
			
			String simpleName = null;
			String datasource = null;
			String id = null;
			
			if(m.matches()) {
				simpleName = m.group(1);
				datasource = m.group(2);
				id = m.group(3);
				if(name.equals(id)) {
					id = null;
				}
			}
			
			// set of genes keys
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			for(int i = 2; i < tokens.length; i++) {
				Integer hash = map.addGene(tokens[i]);
				if(hash != null)
					builder.add(hash);
			}
			
			return GeneSet.createBaderLab(name, description, builder.build(), simpleName, datasource, id);
		}
		return null;
	}
	
}
