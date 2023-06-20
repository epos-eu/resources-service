package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.epos.configuration.repositories.CacheDataRepository;
import org.epos.router_framework.types.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@RestController
public class TNAHelpersApiApiController extends ApiController implements TNAHelpersApi {

	private static final String A_PROBLEM_WAS_ENCOUNTERED_DECODING = "A problem was encountered decoding: ";
	private static final Logger LOGGER = LoggerFactory.getLogger(TNAHelpersApiApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final CacheDataRepository cacheDataRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public TNAHelpersApiApiController(ObjectMapper objectMapper, HttpServletRequest request, CacheDataRepository cacheDataRepository) {
    	super(request);
        this.objectMapper = objectMapper;
        this.request = request;
        this.cacheDataRepository = cacheDataRepository;
    }

    public ResponseEntity<String> tnaDiscoveryGet(@Parameter(in = ParameterIn.QUERY, description = "id" ,schema=@Schema()) @Valid @RequestParam(value = "id", required = true) String id) {
    	
    	if(id==null) {
			return ResponseEntity.badRequest().body("No id parameter provided");
    	}
    	
    	try {
			id=java.net.URLDecoder.decode(id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id, e); 
			return ResponseEntity.badRequest().body(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id);
		}
		Map<String,Object> requestParams = Map.of("id", id);
		
		return standardRequest(ServiceType.METADATA, requestParams, cacheDataRepository);
    }

    public ResponseEntity<String> searchUsingGet(@Parameter(in = ParameterIn.QUERY, description = "q" ,schema=@Schema()) @Valid @RequestParam(value = "q", required = false) String q,@Parameter(in = ParameterIn.QUERY, description = "bbox" ,schema=@Schema()) @Valid @RequestParam(value = "bbox", required = false) String bbox, @Parameter(in = ParameterIn.QUERY, description = "facets" ,schema=@Schema()) @Valid @RequestParam(value = "facets", required = false, defaultValue = "true") boolean facets) {
    	Map<String,Object> requestParameters = new HashMap<>();

		if(!StringUtils.isBlank(q)) {
			try {
				q = java.net.URLDecoder.decode(q, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "q: "+ q, e);
			}
			requestParameters.put("q", q);
		}
		if(!StringUtils.isBlank(bbox)){
			try {
				bbox = java.net.URLDecoder.decode(bbox, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "bbox: "+ bbox, e);
			}
			String[] bboxSplit = bbox.split(",");
			requestParameters.put("epos:northernmostLatitude",bboxSplit[0]);
			requestParameters.put("epos:easternmostLongitude",bboxSplit[1]);
			requestParameters.put("epos:southernmostLatitude",bboxSplit[2]);
			requestParameters.put("epos:westernmostLongitude",bboxSplit[3]);
		}
		
		requestParameters.put("facets", Boolean.toString(facets));
		
		return standardRequest(ServiceType.METADATA, requestParameters, cacheDataRepository);
    }

	@Override
	public ResponseEntity<String> showEquipmentsGet(@Valid String id) {

		if(id==null) {
			return ResponseEntity.badRequest().body("No id parameter provided");
    	}
    	
    	try {
			id=java.net.URLDecoder.decode(id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id, e); 
			return ResponseEntity.badRequest().body(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id);
		}
		Map<String,Object> requestParams = Map.of("id", id);
		
		
		return standardRequest(ServiceType.METADATA, requestParams, cacheDataRepository);
	}
	
	public ResponseEntity<String> showFacilitiesGet(@Valid String id) {

		if(id==null) {
			return ResponseEntity.badRequest().body("No id parameter provided");
    	}
    	
    	try {
			id=java.net.URLDecoder.decode(id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id, e); 
			return ResponseEntity.badRequest().body(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id);
		}
		Map<String,Object> requestParams = Map.of("id", id);
		
		
		return standardRequest(ServiceType.METADATA, requestParams, cacheDataRepository);
	}
	
	private Map<String, Object> decodedRequestParameters(//
			final String id, final String format) {
		final Map<String,Object> requestParameters = new HashMap<>();

		addDecodedParamToMap("id", id, requestParameters ); 
		addDecodedParamToMap("format", format, requestParameters, (key,decoded)-> { 
			String temp = decoded;
			if(temp.equals("application/epos.geo json")) {
				temp = "application/epos.geo+json";
			}
			if(temp.equals("application/epos.table.geo json")) {
				temp = "application/epos.table.geo+json";
			}
			if(temp.equals("application/epos.map.geo json")) {
				temp = "application/epos.map.geo+json";
			}
			if(temp.equals("application/geo json")) {
				temp = "application/geo+json";
			}

			return Set.of( Map.entry(key, temp ));
		}); 

		return requestParameters;
	}
	
	private void addDecodedParamToMap(final String paramKey, final String paramValue,
			final Map<String, Object> requestParameters ) {
		addDecodedParamToMap(paramKey, paramValue, requestParameters, (key,decoded)-> Set.of( Map.entry(key, decoded )));
	}

	private void addDecodedParamToMap(final String paramKey, final String paramValue,
			final Map<String, Object> requestParameters,
			final BiFunction<String, String, Set<Map.Entry<String, String>>> mapDecodedParam) {
		if (!StringUtils.isBlank(paramValue)) {
			String decoded = paramValue;
			try {
				decoded = java.net.URLDecoder.decode(paramValue, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + paramKey + ": " + paramValue, e);
			}

			final Set<Entry<String, String>> entries = mapDecodedParam.apply(paramKey, decoded);

			entries.forEach(e -> {
				requestParameters.put(e.getKey(), e.getValue());
			});
		}
	}
}
