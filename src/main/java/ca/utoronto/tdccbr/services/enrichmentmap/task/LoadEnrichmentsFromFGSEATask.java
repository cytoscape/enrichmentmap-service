package ca.utoronto.tdccbr.services.enrichmentmap.task;

import com.google.common.collect.ImmutableSet;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GeneSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenericResult;

public class LoadEnrichmentsFromFGSEATask implements Task {

	private final EMDataSet dataset;

	public LoadEnrichmentsFromFGSEATask(EMDataSet dataset) {
		this.dataset = dataset;
	}

	@Override
	public void run() {
		var em = dataset.getMap();
		var enrichments = dataset.getEnrichments();
		var genesets = dataset.getSetOfGeneSets().getGeneSets();
		var fgseaRes = dataset.getFGSEARes();
		
		for (var er : fgseaRes) {
			var genes = er.getGenes();
			var name = er.getPathway();
			var description = name;
			var pvalue = er.getPval();
			var qvalue = 1.0;
			var nes = er.getNES();
			
			// Skip row if data is invalid in any way
			if (!(genes == null || genes.isEmpty() || name == null || name.isEmpty())) {
				// Build the GeneSet object
				ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
	
				for (var g : genes) {
					var hash = em.addGene(g);
					
					if (hash != null)
						builder.add(hash);
				}
	
				var gs = new GeneSet(name, description, builder.build());
				int gsSize = gs.getGenes().size();
				genesets.put(name, gs);
	
				var result = new GenericResult(name, description, pvalue, gsSize, qvalue, nes);
				enrichments.getEnrichments().put(name, result);
			}
		}
	}
}
