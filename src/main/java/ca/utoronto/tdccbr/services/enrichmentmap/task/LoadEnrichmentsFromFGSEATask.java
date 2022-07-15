package ca.utoronto.tdccbr.services.enrichmentmap.task;

import ca.utoronto.tdccbr.services.enrichmentmap.model.EMDataSet;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenericResult;

public class LoadEnrichmentsFromFGSEATask implements Task {

	private final EMDataSet dataset;

	public LoadEnrichmentsFromFGSEATask(EMDataSet dataset) {
		this.dataset = dataset;
	}

	@Override
	public void run() {
		var enrichments = dataset.getEnrichments();
		var genesets = dataset.getSetOfGeneSets().getGeneSets();
		var fgseaRes = dataset.getFGSEARes();
		
		for (var er : fgseaRes) {
			var name = er.getPathway();
			var description = name;
			var pvalue = er.getPval();
			var qvalue = 1.0;
			var nes = er.getNES();
	
			// TODO make sure the gene set exists
			int gsSize = genesets.get(name).getGenes().size();
			var result = new GenericResult(name, description, pvalue, gsSize, qvalue, nes);
			enrichments.getEnrichments().put(name, result);
		}
	}
}
