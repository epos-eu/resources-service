package org.epos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.epos.api.utility.Utils;
import org.epos.configuration.repositories.CacheDataRepository;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Organization;
import org.epos.eposdatamodel.Person;
import org.epos.eposdatamodel.Software;
import org.epos.eposdatamodel.WebService;
import org.epos.router_framework.types.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.commons.codec.binary.Base64;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@RestController
public class ResourcesApiController extends ApiController implements ResourcesApi {

	private static final String A_PROBLEM_WAS_ENCOUNTERED_DECODING = "A problem was encountered decoding: ";
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

    private final CacheDataRepository cacheDataRepository;

	@org.springframework.beans.factory.annotation.Autowired
	public ResourcesApiController(ObjectMapper objectMapper, HttpServletRequest request, CacheDataRepository cacheDataRepository) {
		super(request);
		this.objectMapper = objectMapper;
		this.request = request;
        this.cacheDataRepository = cacheDataRepository;
	}

	public ResponseEntity<List<DataProduct>> datasetUsingGet(@Parameter(in = ParameterIn.QUERY, description = "q" ,schema=@Schema()) @Valid @RequestParam(value = "q", required = false) String q,@Parameter(in = ParameterIn.QUERY, description = "startDate" ,schema=@Schema()) @Valid @RequestParam(value = "startDate", required = false) String startDate,@Parameter(in = ParameterIn.QUERY, description = "endDate" ,schema=@Schema()) @Valid @RequestParam(value = "endDate", required = false) String endDate,@Parameter(in = ParameterIn.QUERY, description = "bbox" ,schema=@Schema()) @Valid @RequestParam(value = "bbox", required = false) String bbox,@Parameter(in = ParameterIn.QUERY, description = "keywords" ,schema=@Schema()) @Valid @RequestParam(value = "keywords", required = false) String keywords,@Parameter(in = ParameterIn.QUERY, description = "organisations" ,schema=@Schema()) @Valid @RequestParam(value = "organisations", required = false) String organisations) {

		Map<String,Object> requestParameters = new HashMap<>();

		if(!StringUtils.isBlank(q)) {
			try {
				q = java.net.URLDecoder.decode(q, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "q: "+ q, e);
			}
			requestParameters.put("q", q);
		}
		if(!StringUtils.isBlank(startDate)) {
			try {
				startDate = java.net.URLDecoder.decode(startDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
			}
			try {
				requestParameters.put("schema:startDate", Utils.convert(startDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format));
			} catch (ParseException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
			}
		}
		if(!StringUtils.isBlank(endDate)) {
			try {
				endDate = java.net.URLDecoder.decode(endDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
			}
			try {
				requestParameters.put("schema:endDate", Utils.convert(endDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format));
			} catch (ParseException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
			}
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
		if(!StringUtils.isBlank(keywords)) {
			try {
				keywords=java.net.URLDecoder.decode(keywords, StandardCharsets.UTF_8.name());
				keywords = keywords.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "keywords: "+ keywords, e);
			}
			ArrayList<String> kws = new ArrayList<String>();
			for(String kw : keywords.split("\\,")) {
				byte[] byteArray = Base64.decodeBase64(kw.getBytes());
				kws.add(new String(byteArray));
			}
			requestParameters.put("keywords", kws.toString().replace("[", "").replace("]", ""));
		}
		if(!StringUtils.isBlank(organisations)) {
			try {
				organisations=java.net.URLDecoder.decode(organisations, StandardCharsets.UTF_8.name());
				organisations = organisations.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "organisations: "+ organisations, e);
			}
			requestParameters.put("organisations", organisations);
		}

		// < validation >
		try {
			if(!StringUtils.isBlank(startDate)) Utils.convert(startDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format);
			if(!StringUtils.isBlank(endDate)) Utils.convert(endDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format);
		} catch (ParseException e1) { 
			LOGGER.error(String.format("Invalid date format specified (use the format '%s')", Utils.EPOS_internal_format),e1);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return standardRequest(ServiceType.METADATA, requestParameters, cacheDataRepository);
	}

	public ResponseEntity<List<Software>> softwareUsingGet() {
		Map<String,Object> requestParameters = new HashMap<>();

		return standardRequest(ServiceType.METADATA, requestParameters, cacheDataRepository);
	}

	public ResponseEntity<List<WebService>> webserviceUsingGet(@Parameter(in = ParameterIn.QUERY, description = "q" ,schema=@Schema()) @Valid @RequestParam(value = "q", required = false) String q,@Parameter(in = ParameterIn.QUERY, description = "startDate" ,schema=@Schema()) @Valid @RequestParam(value = "startDate", required = false) String startDate,@Parameter(in = ParameterIn.QUERY, description = "endDate" ,schema=@Schema()) @Valid @RequestParam(value = "endDate", required = false) String endDate,@Parameter(in = ParameterIn.QUERY, description = "bbox" ,schema=@Schema()) @Valid @RequestParam(value = "bbox", required = false) String bbox,@Parameter(in = ParameterIn.QUERY, description = "keywords" ,schema=@Schema()) @Valid @RequestParam(value = "keywords", required = false) String keywords,@Parameter(in = ParameterIn.QUERY, description = "organisations" ,schema=@Schema()) @Valid @RequestParam(value = "organisations", required = false) String organisations) {
		Map<String,Object> requestParameters = new HashMap<>();

		if(!StringUtils.isBlank(q)) {
			try {
				q = java.net.URLDecoder.decode(q, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "q: "+ q, e);
			}
			requestParameters.put("q", q);
		}
		if(!StringUtils.isBlank(startDate)) {
			try {
				startDate = java.net.URLDecoder.decode(startDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
			}
			try {
				requestParameters.put("schema:startDate", Utils.convert(startDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format));
			} catch (ParseException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "startDate: "+ startDate, e);
			}
		}
		if(!StringUtils.isBlank(endDate)) {
			try {
				endDate = java.net.URLDecoder.decode(endDate, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
			}
			try {
				requestParameters.put("schema:endDate", Utils.convert(endDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format));
			} catch (ParseException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "endDate: "+ endDate, e);
			}
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
		if(!StringUtils.isBlank(keywords)) {
			try {
				keywords=java.net.URLDecoder.decode(keywords, StandardCharsets.UTF_8.name());
				keywords = keywords.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "keywords: "+ keywords, e);
			}
			ArrayList<String> kws = new ArrayList<String>();
			for(String kw : keywords.split("\\,")) {
				byte[] byteArray = Base64.decodeBase64(kw.getBytes());
				kws.add(new String(byteArray));
			}
			requestParameters.put("keywords", kws.toString().replace("[", "").replace("]", ""));
		}
		if(!StringUtils.isBlank(organisations)) {
			try {
				organisations=java.net.URLDecoder.decode(organisations, StandardCharsets.UTF_8.name());
				organisations = organisations.replace(" ", "");
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "organisations: "+ organisations, e);
			}
			ArrayList<String> ors = new ArrayList<String>();
			for(String or : organisations.split("\\,")) {
				byte[] byteArray = Base64.decodeBase64(or.getBytes());
				ors.add(new String(byteArray));
			}
			requestParameters.put("organisations", ors.toString().replace("[", "").replace("]", ""));
		}

		// < validation >
		try {
			if(!StringUtils.isBlank(startDate)) Utils.convert(startDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format);
			if(!StringUtils.isBlank(endDate)) Utils.convert(endDate, Utils.EPOS_internal_format, Utils.EPOS_internal_format);
		} catch (ParseException e1) { 
			LOGGER.error(String.format("Invalid date format specified (use the format '%s')", Utils.EPOS_internal_format),e1);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// < /validation >

		return standardRequest(ServiceType.METADATA, requestParameters, cacheDataRepository);
	}

	@Override
	public ResponseEntity<List<Organization>> organisationUsingGet(@Parameter(in = ParameterIn.QUERY, description = "the id of organisation" ,required=false,schema=@Schema()) @Valid @RequestParam(value = "id", required = false) String id) {
		Map<String,Object> requestParams = new HashMap<String, Object>();
		if(id!=null) {
			try {
				id=java.net.URLDecoder.decode(id, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(A_PROBLEM_WAS_ENCOUNTERED_DECODING + "id: "+ id, e); 
			}
			requestParams = Map.of("id", id);
		}

		return standardRequest(ServiceType.METADATA, requestParams, cacheDataRepository);
	}

}
