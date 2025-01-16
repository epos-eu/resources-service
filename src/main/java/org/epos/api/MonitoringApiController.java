package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.epos.api.beans.MonitoringBean;
import org.epos.api.core.MonitoringGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@RestController
public class MonitoringApiController extends ApiController implements MonitoringApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;


	@org.springframework.beans.factory.annotation.Autowired
	public MonitoringApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		super(request);
		this.objectMapper = objectMapper;
		this.request = request;
	}
	
	@Override
	public ResponseEntity<List<MonitoringBean>> monitoringUsingGet() {
	        
		return standardRequest("MONITORING", null, null);
	}
}