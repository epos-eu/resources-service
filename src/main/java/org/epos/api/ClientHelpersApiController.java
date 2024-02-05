package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.epos.api.beans.Distribution;
import org.epos.api.beans.SearchResponse;
import org.epos.api.utility.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@RestController
public class ClientHelpersApiController extends ApiController implements ClientHelpersApi {

	private static final String A_PROBLEM_WAS_ENCOUNTERED_DECODING = "A problem was encountered decoding: ";
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHelpersApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public ClientHelpersApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		super(request);
		this.objectMapper = objectMapper;
		this.request = request;
	}

	public ResponseEntity<Distribution> resourcesDiscoveryGetUsingGET(@Parameter(in = ParameterIn.PATH, description = "The distribution ID", required=true, schema=@Schema()) @PathVariable("instance_id") String id){

		if(id==null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			id=java.net.URLDecoder.decode(id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id, e); 
			Distribution errorResponse = new Distribution(e.getLocalizedMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
		Map<String,Object> requestParams = Map.of("id", id);

		return standardRequest("DETAILS", requestParams);
	}

	public ResponseEntity<SearchResponse> searchUsingGet(@Parameter(in = ParameterIn.QUERY, description = "q" ,schema=@Schema()) @Valid @RequestParam(value = "q", required = false) String q, 
			@Parameter(in = ParameterIn.QUERY, description = "startDate" ,schema=@Schema()) @Valid @RequestParam(value = "startDate", required = false) String startDate,
			@Parameter(in = ParameterIn.QUERY, description = "endDate" ,schema=@Schema()) @Valid @RequestParam(value = "endDate", required = false) String endDate, 
			@Parameter(in = ParameterIn.QUERY, description = "bbox" ,schema=@Schema()) @Valid @RequestParam(value = "bbox", required = false) String bbox, 
			@Parameter(in = ParameterIn.QUERY, description = "keywords" ,schema=@Schema()) @Valid @RequestParam(value = "keywords", required = false) String keywords, 
			@Parameter(in = ParameterIn.QUERY, description = "sciencedomains" ,schema=@Schema()) @Valid @RequestParam(value = "sciencedomains", required = false) String sciencedomains,
			@Parameter(in = ParameterIn.QUERY, description = "servicetypes" ,schema=@Schema()) @Valid @RequestParam(value = "servicetypes", required = false) String servicetypes,
			@Parameter(in = ParameterIn.QUERY, description = "organisations" ,schema=@Schema()) @Valid @RequestParam(value = "organisations", required = false) String organisations,
			@Parameter(in = ParameterIn.QUERY, description = "facetstype {categories, dataproviders, serviceproviders}" ,schema=@Schema(allowableValues = {"categories", "dataproviders", "serviceproviders"})) @Valid @RequestParam(value = "facetstype", required = false) String facetsType,
			@Parameter(in = ParameterIn.QUERY, description = "facets" ,schema=@Schema()) @Valid @RequestParam(value = "facets", required = false) Boolean facets) {
		Map<String,Object> requestParameters = new HashMap<>();

		if(!StringUtils.isBlank(q)) {
			try {
				q = java.net.URLDecoder.decode(q, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "q: "+ q, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			requestParameters.put("q", q);
		}
		if(!StringUtils.isBlank(startDate)) {
			try {
				startDate = java.net.URLDecoder.decode(startDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			try {
				String date = Utils.convertDateUsingPattern(startDate, Utils.EPOSINTERNALFORMAT, Utils.EPOSINTERNALFORMAT);
				if(date==null){
					SearchResponse errorResponse = new SearchResponse("Encountered an error parsing or managing the following DateTime: "+startDate);
					return ResponseEntity.badRequest().body(errorResponse);
				}else {
					requestParameters.put("schema:startDate", date);
				}
			} catch (IllegalArgumentException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
		}
		if(!StringUtils.isBlank(endDate)) {
			try {
				endDate = java.net.URLDecoder.decode(endDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			try {
				String date = Utils.convertDateUsingPattern(endDate, Utils.EPOSINTERNALFORMAT, Utils.EPOSINTERNALFORMAT);
				if(date==null){
					SearchResponse errorResponse = new SearchResponse("Encountered an error parsing or managing the following DateTime: "+endDate);
					return ResponseEntity.badRequest().body(errorResponse);
				}else {
					requestParameters.put("schema:endDate", date);
				}
			} catch (IllegalArgumentException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
		}
		if(!StringUtils.isBlank(bbox)){
			try {
				bbox = java.net.URLDecoder.decode(bbox, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "bbox: "+ bbox, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			try {
				String[] bboxSplit = bbox.split(",");
				requestParameters.put("epos:northernmostLatitude",bboxSplit[0]);
				requestParameters.put("epos:easternmostLongitude",bboxSplit[1]);
				requestParameters.put("epos:southernmostLatitude",bboxSplit[2]);
				requestParameters.put("epos:westernmostLongitude",bboxSplit[3]);
			} catch (ArrayIndexOutOfBoundsException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "bbox: "+ bbox, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
		}
		if(!StringUtils.isBlank(keywords)) {
			try {
				keywords=java.net.URLDecoder.decode(keywords, StandardCharsets.UTF_8.name());
				keywords = keywords.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "keywords: "+ keywords, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			requestParameters.put("keywords", keywords);
		}
		if(!StringUtils.isBlank(sciencedomains)) {
			try {
				sciencedomains=java.net.URLDecoder.decode(sciencedomains, StandardCharsets.UTF_8.name());
				sciencedomains = sciencedomains.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "sciencedomains: "+ sciencedomains, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			requestParameters.put("sciencedomains", sciencedomains);
		}
		if(!StringUtils.isBlank(servicetypes)) {
			try {
				servicetypes=java.net.URLDecoder.decode(servicetypes, StandardCharsets.UTF_8.name());
				servicetypes = servicetypes.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "servicetypes: "+ servicetypes, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			requestParameters.put("servicetypes", servicetypes);
		}
		if(!StringUtils.isBlank(organisations)) {
			try {
				organisations=java.net.URLDecoder.decode(organisations, StandardCharsets.UTF_8.name());
				organisations = organisations.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "organisations: "+ organisations, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			requestParameters.put("organisations", organisations);
		}


		if(facets==null) requestParameters.put("facets", System.getenv("FACETS_DEFAULT"));
		else requestParameters.put("facets", Boolean.toString(facets));

		if(facetsType==null) requestParameters.put("facetstype", System.getenv("FACETS_TYPE_DEFAULT"));
		else {
			try {
				facetsType=java.net.URLDecoder.decode(facetsType, StandardCharsets.UTF_8.name());
				facetsType = facetsType.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "facetsType: "+ facetsType, e);
				SearchResponse errorResponse = new SearchResponse(e.getLocalizedMessage());
				return ResponseEntity.badRequest().body(errorResponse);
			}
			if(!(facetsType.equals("categories") || facetsType.equals("dataproviders") || facetsType.equals("serviceproviders"))) {
				SearchResponse errorResponse = new SearchResponse("The facets type is not a valid type, supported types: categories, dataproviders, serviceproviders]");
				return ResponseEntity.badRequest().body(errorResponse);

			}
			requestParameters.put("facetstype", facetsType);
		}

		if(!StringUtils.isBlank(startDate)) {
			if(Utils.convertDateUsingPattern(startDate, Utils.EPOSINTERNALFORMAT, Utils.EPOSINTERNALFORMAT)==null) {
				SearchResponse errorResponse = new SearchResponse("Encountered an error parsing or managing the following DateTime: "+startDate);
				return ResponseEntity.badRequest().body(errorResponse);
			}
		}
		if(!StringUtils.isBlank(endDate)) {
			if(Utils.convertDateUsingPattern(endDate, Utils.EPOSINTERNALFORMAT, Utils.EPOSINTERNALFORMAT)==null){
				SearchResponse errorResponse = new SearchResponse("Encountered an error parsing or managing the following DateTime: "+endDate);
				return ResponseEntity.badRequest().body(errorResponse);
			}
		}

		return standardRequest("SEARCH", requestParameters);
	}

}
