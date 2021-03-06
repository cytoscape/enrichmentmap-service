package ca.utoronto.tdccbr.services.enrichmentmap.service;

import static ca.utoronto.tdccbr.services.enrichmentmap.model.Columns.*;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Service;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.EdgeDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.EdgeDataDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.NetworkDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.NodeDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.NodeDataDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.RequestDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.ResultDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.model.CyNetwork;
import ca.utoronto.tdccbr.services.enrichmentmap.model.CyTable.CyRow;
import ca.utoronto.tdccbr.services.enrichmentmap.model.EnrichmentMap;
import ca.utoronto.tdccbr.services.enrichmentmap.model.GenesetSimilarity;
import ca.utoronto.tdccbr.services.enrichmentmap.model.SimilarityKey;
import ca.utoronto.tdccbr.services.enrichmentmap.task.ComputeSimilarityTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.CreateEMNetworkTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.FilterGenesetsByDatasetGenesTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.GMTFileReaderTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.InitializeGenesetsOfInterestTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.LoadEnrichmentsFromFGSEATask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.ModelCleanupTask;
import ca.utoronto.tdccbr.services.enrichmentmap.task.Task;
import ca.utoronto.tdccbr.services.enrichmentmap.util.Baton;

@Service
public class EMService {
	

	public ResultDTO createNetwork(RequestDTO request) {
		var params = request.getParameters();
		var dsParams = request.getDataSets();
		var em = new EnrichmentMap(params);
		
		var tasks = new ArrayList<Task>();
		
		for (var dsp : dsParams) {
			// NOTE: We only support FGSEA for now.
			var dsName = dsp.getName();
			var fgseaRes = dsp.getFgseaResults();

			// Create Data Set and load the enrichments from the input data
			var ds = em.createDataSet(dsName, fgseaRes);
			
			tasks.add(new GMTFileReaderTask(ds, GMTFileReaderTask.DATASET_NAME_1));
			
			tasks.add(new LoadEnrichmentsFromFGSEATask(ds));
		}
		
		// Filter out genesets that don't pass the p-value and q-value thresholds
		tasks.add(new InitializeGenesetsOfInterestTask(em));
		
		// Trim the genesets to only contain the genes that are in the data file.
		tasks.add(new FilterGenesetsByDatasetGenesTask(em));
		
		var pipe = new Baton<Map<SimilarityKey, GenesetSimilarity>>();

		// Compute the geneset similarities
		tasks.add(new ComputeSimilarityTask(em, pipe.consumer()));

		// Create the network
		var netTask = new CreateEMNetworkTask(em, pipe.supplier());
		tasks.add(netTask);

		// Make any final adjustments to the model
		tasks.add(new ModelCleanupTask(em)); // TODO probably not necessary
		
		for (var t : tasks) {
			try {
				t.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		var network = netTask.getResults(CyNetwork.class);
		var netDto = createNetworkDTO(network);
		
		return new ResultDTO(em.getParams(), netDto);
	}
	
	private static NetworkDTO createNetworkDTO(CyNetwork net) {
		var netDto = new NetworkDTO();
		var elementsDto = netDto.getElements();
		var nodeList = elementsDto.getNodes();
		var edgeList = elementsDto.getEdges();
		
		for (var n : net.getNodeList()) {
			var nd = new NodeDTO(n.getId());
			nodeList.add(nd);
			
			// Set EM the attributes
			var row = net.getRow(n);
			var data = nd.getData();
			copyAttributes(row, data);
		}
		
		for (var e : net.getEdgeList()) {
			var ed = new EdgeDTO(e.getId(), e.getSource().getId(), e.getTarget().getId());
			edgeList.add(ed);
			
			// Set EM the attributes
			var row = net.getRow(e);
			var data = ed.getData();
			copyAttributes(row, data);
		}
			
		return netDto;
	}
	
	private static void copyAttributes(CyRow row, NodeDataDTO dto) {
		dto.setName(NODE_NAME.get(row, NAMESPACE_PREFIX));
		dto.setGsType(NODE_GS_TYPE.get(row, NAMESPACE_PREFIX));
		dto.setGsSize(NODE_GS_SIZE.get(row, NAMESPACE_PREFIX));
		dto.setPvalue(NODE_PVALUE.get(row, NAMESPACE_PREFIX));
		dto.setFdrQvalue(NODE_FDR_QVALUE.get(row, NAMESPACE_PREFIX));
		dto.setNES(NODE_NES.get(row, NAMESPACE_PREFIX));
		dto.setColouring(NODE_COLOURING.get(row, NAMESPACE_PREFIX));
	}
	
	private static void copyAttributes(CyRow row, EdgeDataDTO dto) {
		dto.setSimilarityCoefficient(EDGE_SIMILARITY_COEFF.get(row, NAMESPACE_PREFIX));
		dto.setOverlapSize(EDGE_OVERLAP_SIZE.get(row, NAMESPACE_PREFIX));
		// NOTE: Not sending the "Data Set" attribute, because this service supports only one dataset
	}
}
