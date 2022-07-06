package ca.utoronto.tdccbr.services.enrichmentmap.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.utoronto.tdccbr.services.enrichmentmap.dto.RequestDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.dto.ResultDTO;
import ca.utoronto.tdccbr.services.enrichmentmap.service.EMService;

@RestController
@RequestMapping("/")
public class EMController {
	
	@Autowired
	private EMService emService;
	
	@PostMapping(
			value = "/v1",
			consumes = MediaType.APPLICATION_JSON_VALUE,
	        produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResultDTO createEMNetwork(@RequestBody RequestDTO request) {
		var result = emService.createNetwork(request);
		
		return result;
	}
	
	/**
     * Redirects the index to the Swagger UI.
     */
	@GetMapping("/")
    public String index(HttpServletResponse resp) throws Exception {
    	resp.sendRedirect("/swagger-ui/index.html");
        
    	return null;
    }
	
	/**
	 * This endpoint can be used for monitoring.
	 * If the service is running, the response is an HTTP status code of 200 and a simple "OK" text.
	 */
	@GetMapping(value = "/ping")
	public String ping() {
		return "OK";
	}
}
