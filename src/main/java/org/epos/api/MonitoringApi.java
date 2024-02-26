package org.epos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import org.epos.api.beans.MonitoringBean;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@Validated
public interface MonitoringApi {

	@Operation(summary = "monitoring operation", description = "Monitoring endpoint", tags={ "Resources Service" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "ok.", content = @Content(mediaType = "*/*", schema = @Schema(implementation = MonitoringBean.class))),

			@ApiResponse(responseCode = "201", description = "Created."),

			@ApiResponse(responseCode = "204", description = "No content."),

			@ApiResponse(responseCode = "301", description = "Moved Permanently."),

			@ApiResponse(responseCode = "400", description = "Bad request."),

			@ApiResponse(responseCode = "401", description = "Access token is missing or invalid"),

			@ApiResponse(responseCode = "403", description = "Forbidden"),

			@ApiResponse(responseCode = "404", description = "Not Found") })
	@RequestMapping(value = "/resources/monitoring",
	produces = { "*/*" }, 
	method = {RequestMethod.GET,RequestMethod.OPTIONS})
	ResponseEntity<List<MonitoringBean>> monitoringUsingGet();

}
