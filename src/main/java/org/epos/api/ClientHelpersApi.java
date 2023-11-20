/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.29).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.epos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.epos.api.beans.Distribution;
import org.epos.api.beans.SearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@Validated
public interface ClientHelpersApi {

	@Operation(summary = "metadata resources details", description = "returns detailed information useful to contextualise the discovery phase", tags={ "Resources Service" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "ok.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Distribution.class))),
        
        @ApiResponse(responseCode = "201", description = "Created."),
        
        @ApiResponse(responseCode = "204", description = "No content."),
        
        @ApiResponse(responseCode = "301", description = "Moved Permanently."),
        
        @ApiResponse(responseCode = "400", description = "Bad request."),
        
        @ApiResponse(responseCode = "401", description = "Access token is missing or invalid"),
        
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        
        @ApiResponse(responseCode = "404", description = "Not Found") })
    @RequestMapping(value = "/resources/details",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Distribution> resourcesDiscoveryGetUsingGET(@NotNull @Parameter(in = ParameterIn.QUERY, description = "The distribution ID" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "id", required = true) String id);


	@Operation(summary = "search operation", description = "Search endpoint", tags={ "Resources Service" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "ok.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchResponse.class))),

			@ApiResponse(responseCode = "201", description = "Created."),

			@ApiResponse(responseCode = "204", description = "No content."),

			@ApiResponse(responseCode = "301", description = "Moved Permanently."),
			
			@ApiResponse(responseCode = "400", description = "Bad request."),

			@ApiResponse(responseCode = "401", description = "Access token is missing or invalid"),

			@ApiResponse(responseCode = "403", description = "Forbidden"),

			@ApiResponse(responseCode = "404", description = "Not Found") })
	@RequestMapping(value = "/resources/search",
	produces = { "application/json" }, 
	method = RequestMethod.GET)
	ResponseEntity<SearchResponse> searchUsingGet(@Parameter(in = ParameterIn.QUERY, description = "q" ,schema=@Schema()) @Valid @RequestParam(value = "q", required = false) String q, 
			@Parameter(in = ParameterIn.QUERY, description = "startDate" ,schema=@Schema()) @Valid @RequestParam(value = "startDate", required = false) String startDate,
			@Parameter(in = ParameterIn.QUERY, description = "endDate" ,schema=@Schema()) @Valid @RequestParam(value = "endDate", required = false) String endDate, 
			@Parameter(in = ParameterIn.QUERY, description = "bbox" ,schema=@Schema()) @Valid @RequestParam(value = "bbox", required = false) String bbox, 
			@Parameter(in = ParameterIn.QUERY, description = "keywords" ,schema=@Schema()) @Valid @RequestParam(value = "keywords", required = false) String keywords, 
			@Parameter(in = ParameterIn.QUERY, description = "sciencedomains" ,schema=@Schema()) @Valid @RequestParam(value = "sciencedomains", required = false) String sciencedomains,
			@Parameter(in = ParameterIn.QUERY, description = "servicetypes" ,schema=@Schema()) @Valid @RequestParam(value = "servicetypes", required = false) String servicetypes,
			@Parameter(in = ParameterIn.QUERY, description = "organisations" ,schema=@Schema()) @Valid @RequestParam(value = "organisations", required = false) String organisations,
			@Parameter(in = ParameterIn.QUERY, description = "facetstype {categories, dataproviders, serviceproviders}" ,schema=@Schema()) @Valid @RequestParam(value = "facetstype", required = false) String facetsType,
			@Parameter(in = ParameterIn.QUERY, description = "facets" ,schema=@Schema()) @Valid @RequestParam(value = "facets", required = false) Boolean facets);

}

