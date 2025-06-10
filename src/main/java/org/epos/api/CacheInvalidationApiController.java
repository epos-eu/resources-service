package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.EposDataModelDAO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.epos.api.beans.Distribution;
import org.epos.api.beans.LinkedResponse;
import org.epos.api.beans.ParametersResponse;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.distributions.LinkedEntityParametersSearch;
import org.epos.api.core.distributions.LinkedEntityWebserviceSearch;
import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@RestController
public class CacheInvalidationApiController extends ApiController implements CacheInvalidationApi {

	private static final String A_PROBLEM_WAS_ENCOUNTERED_DECODING = "A problem was encountered decoding: ";
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidationApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public CacheInvalidationApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		super(request);
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@Override
	public ResponseEntity<Object> resourcesInvalidationCache() {
		EposDataModelDAO.clearAllCaches();
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
