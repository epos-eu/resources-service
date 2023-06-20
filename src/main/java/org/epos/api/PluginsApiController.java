package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.epos.api.clienthelpers.model.PluginResource;
import org.epos.configuration.repositories.CacheDataRepository;
import org.epos.router_framework.types.ServiceType;
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
public class PluginsApiController extends ApiController implements PluginsApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

    private final CacheDataRepository cacheDataRepository;

	@org.springframework.beans.factory.annotation.Autowired
	public PluginsApiController(ObjectMapper objectMapper, HttpServletRequest request, CacheDataRepository cacheDataRepository) {
		super(request);
		this.objectMapper = objectMapper;
		this.request = request;
		this.cacheDataRepository = cacheDataRepository;
	}
	

	@Override
	public ResponseEntity<List<PluginResource>> pluginsUsingGet() {
	        
		Map<String,Object> requestParameters = new HashMap<>();
		return standardRequest(ServiceType.METADATA, requestParameters, cacheDataRepository);
	}
}
