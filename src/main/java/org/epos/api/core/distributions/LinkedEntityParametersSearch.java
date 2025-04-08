package org.epos.api.core.distributions;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.epos.api.beans.Parameter;
import org.epos.api.beans.ParametersResponse;
import org.epos.api.routines.DatabaseConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedEntityParametersSearch {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedEntityParametersSearch.class);

	public static ParametersResponse generate(String id) {
		LOGGER.info("Starting parameter search for distribution id: {}", id);
		var db = DatabaseConnections.getInstance();

		// find the distribution
		var distribution = db.getDistributionList().stream()
				.filter(d -> d.getInstanceId().equals(id))
				.findFirst()
				.orElse(null);

		if (distribution == null || distribution.getSupportedOperation() == null
				|| distribution.getSupportedOperation().isEmpty()) {
			LOGGER.warn("Distribution {} not found or has no supported operations.", id);
			return null;
		}
		LOGGER.debug("Found distribution {} with supported operations.", id);

		// assuming only one operation for a distribution
		String operationId = distribution.getSupportedOperation().get(0).getInstanceId();
		LOGGER.debug("Using operation id {} for distribution {}.", operationId, id);

		// find the operation
		var operation = db.getOperationList().stream()
				.filter(op -> op.getInstanceId().equals(operationId))
				.findFirst()
				.orElse(null);

		if (operation == null || operation.getPayload() == null || operation.getPayload().isEmpty()) {
			LOGGER.warn("Operation {} not found or has no payload for distribution {}.", operationId, id);
			return null;
		}
		LOGGER.debug("Operation {} found with payload.", operationId);

		// assuming only one payload for an operation
		String payloadId = operation.getPayload().get(0).getInstanceId();
		LOGGER.debug("Using payload id {} for operation {}.", payloadId, operationId);

		// find the payload
		var payload = db.getPayloads().stream()
				.filter(p -> p.getInstanceId().equals(payloadId))
				.findFirst()
				.orElse(null);

		if (payload == null || payload.getOutputMapping() == null) {
			LOGGER.warn("Payload {} not found or has no output mapping for operation {}.", payloadId, operationId);
			return null;
		}
		LOGGER.debug("Payload {} found with output mappings.", payloadId);

		// get the output mappings for this payload
		var parameters = new HashSet<Parameter>();
		var relevantMappingIds = payload.getOutputMapping().stream()
				.map(mapping -> mapping.getInstanceId())
				.collect(Collectors.toSet());
		LOGGER.debug("Relevant mapping ids: {}", relevantMappingIds);

		// create the parameters only for the relevant mappings
		db.getOutputMappings().stream()
				.filter(mapping -> relevantMappingIds.contains(mapping.getInstanceId()))
				.forEach(mapping -> {
					parameters.add(new Parameter(mapping.getOutputProperty(), mapping.getOutputVariable()));
					LOGGER.debug("Added parameter: {} -> {}", mapping.getOutputProperty(), mapping.getOutputVariable());
				});

		LOGGER.info("Successfully found {} parameters for distribution id {}.", parameters.size(), id);
		return new ParametersResponse(parameters);
	}
}
