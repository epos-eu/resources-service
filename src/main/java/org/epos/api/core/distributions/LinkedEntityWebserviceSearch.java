package org.epos.api.core.distributions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.beans.LinkedResponse;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.Mapping;
import org.epos.eposdatamodel.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LinkedEntityWebserviceSearch
 */
public class LinkedEntityWebserviceSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedEntityWebserviceSearch.class);
	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/resources/details/";

	public static LinkedResponse generate(String id, Set<String> params) {
		var results = generateDiscoveryItems(params);
		results.removeIf(d -> d.getId().equals(id));
		return new LinkedResponse(results);
	}

	public static LinkedResponse generate(Set<String> params) {
		return new LinkedResponse(generateDiscoveryItems(params));
	}

	private static Set<DiscoveryItem> generateDiscoveryItems(Set<String> params) {
		var discoveryMap = new HashSet<DiscoveryItem>();
		var db = DatabaseConnections.getInstance();
		var distributions = db.getDistributionList();
		Map<String, Mapping> mappings = db.getMappingList().stream()
				.collect(Collectors.toMap(
						Mapping::getInstanceId,
						Function.identity(),
						(existing, replacement) -> existing // merge function - handles duplicates
				));
		Map<String, Operation> operations = db.getOperationList().stream()
				.collect(Collectors.toMap(
						Operation::getInstanceId,
						Function.identity(),
						(existing, replacement) -> existing // merge function - handles duplicates
				));

		for (var distribution : distributions) {
			var valid = false;
			// take it only if it has an operation that has all the mappings that are in the
			// input parameters
			if (distribution.getSupportedOperation() == null) {
				continue;
			}
			for (var linkedOperation : distribution.getSupportedOperation()) {
				var operation = operations.get(linkedOperation.getInstanceId());
				if (operation == null) {
					continue;
				}

				if (operation.getMapping() == null) {
					continue;
				}

				// store all the mappings for this operation
				var mappingProperties = new HashSet<String>();
				for (var linkedMapping : operation.getMapping()) {
					var mapping = mappings.get(linkedMapping.getInstanceId());
					if (mapping == null) {
						continue;
					}

					// this is a mapping that is part of the operation of the distribution
					mappingProperties.add(mapping.getProperty());
				}

				// all the mappings in the params must be available for this operation
				if (mappingProperties.containsAll(params)) {
					valid = true;
				}
			}

			if (valid == false) {
				continue;
			}

			// get the available formats
			List<AvailableFormat> availableFormats = AvailableFormatsGeneration
					.generate(distribution);

			DiscoveryItem discoveryItem = new DiscoveryItemBuilder(
					distribution.getInstanceId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId() + "?extended=true")
					.uid(distribution.getUid())
					.setMetaId(distribution.getMetaId())
					.title(distribution.getTitle() != null ? String.join(";", distribution.getTitle()) : null)
					.description(distribution.getDescription() != null ? String.join(";", distribution.getDescription())
							: null)
					.availableFormats(availableFormats)
					.setVersioningStatus(distribution.getStatus().name())
					.build();

			if (EnvironmentVariables.MONITORING != null && EnvironmentVariables.MONITORING.equals("true")) {
				discoveryItem
						.setStatus(ZabbixExecutor.getInstance().getStatusInfoFromSha(discoveryItem.getSha256id()));
				discoveryItem.setStatusTimestamp(
						ZabbixExecutor.getInstance().getStatusTimestampInfoFromSha(discoveryItem.getSha256id()));
			}

			discoveryMap.add(discoveryItem);
		}

		LOGGER.info("Final number of results: {}", discoveryMap.size());

		return discoveryMap;
	}
}
