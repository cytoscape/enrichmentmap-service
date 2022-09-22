package ca.utoronto.tdccbr.services.enrichmentmap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.adelean.inject.resources.junit.jupiter.GivenTextResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.utoronto.tdccbr.services.enrichmentmap.controller.EMController;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.DataSetParametersDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.EMCreationParametersDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.FGSEAEnrichmentResultDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.RequestDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.ResultDTO;

@ContextConfiguration(classes = { EMController.class, EMService.class })
@AutoConfigureMockMvc
@WebMvcTest
@TestWithResources
public class EMServiceTests {

	private static final String FGSEA_FILENAME = "fgsea-results.json";
	
	@GivenTextResource(FGSEA_FILENAME)
	String fgseaResJson;
	
	private List<FGSEAEnrichmentResultDTO> fgseaResults;
	
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	
	private JacksonTester<RequestDTO> reqDTOJsonTester;
	
	private DataSetParametersDTO dsParams;
	private RequestDTO reqDTO;

	@BeforeEach
    public void setup() throws Exception {
        JacksonTester.initFields(this, objectMapper);
        
		fgseaResults = objectMapper.readValue(fgseaResJson, new TypeReference<List<FGSEAEnrichmentResultDTO>>() {});
        
        dsParams = new DataSetParametersDTO(FGSEA_FILENAME, fgseaResults);
        var emParams = new EMCreationParametersDTO();
        emParams.setFDR(false); // turn off q-value filtering for this test
		reqDTO = new RequestDTO(emParams, Collections.singletonList(dsParams));
    }
	
	@Test
    public void testPing() throws Exception {
		var mvcRes = mockMvc.perform(get("/ping"))
				.andExpect(status().isOk())
                .andReturn();
		
		var s = mvcRes.getResponse().getContentAsString();
		assertEquals("OK", s);
	}

	@Test
    public void testCreateEMNetwork() throws Exception {
		var reqJson = reqDTOJsonTester.write(reqDTO).getJson();
		
		var mvcRes = mockMvc.perform(post("/v1")
                .content(reqJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

		var s = mvcRes.getResponse().getContentAsString();
		var res = objectMapper.readValue(s, ResultDTO.class);
		
		// Check the Response
		var params = res.getParameters();
		assertNotNull(params);
		var net = res.getNetwork();
		assertNotNull(net);
		
		// Check the EM Network
		assertEquals(5, net.getElements().getNodes().size());
		assertEquals(8, net.getElements().getEdges().size());
		
		// Check EM columns
		// TODO
	}
}
