package ca.utoronto.tdccbr.services.enrichmentmap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.RequestDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.ResultDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.service.EMService;

@RestController
@RequestMapping("/networks")
public class EMController {
	
	@Autowired
	private EMService emService;
	
	@PostMapping(
			consumes = MediaType.APPLICATION_JSON_VALUE,
	        produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResultDTO createEMNetwork(@RequestBody RequestDTO request) {
		var result = emService.createNetwork(request);
		
		return result;
	}
}
