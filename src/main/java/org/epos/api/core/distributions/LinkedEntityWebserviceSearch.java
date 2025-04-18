package org.epos.api.core.distributions;

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
import org.epos.eposdatamodel.Distribution;
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

	public static LinkedResponse generate(String id, Map<String, String> params) {
		LOGGER.info("Generating discovery items with exclusion id {} and params {}", id, params);
		var results = generateDiscoveryItems(params);
		results.removeIf(d -> d.getId().equals(id));
		return new LinkedResponse(results);
	}

	public static LinkedResponse generate(Map<String, String> params) {
		LOGGER.info("Generating discovery items with params {}", params);
		return new LinkedResponse(generateDiscoveryItems(params));
	}

	// filter all the distributions keeping only the ones that have all the mappings
	// in the params
	private static Set<DiscoveryItem> generateDiscoveryItems(Map<String, String> params) {
		var db = DatabaseConnections.getInstance();
		var distributions = db.getDistributionList();
		LOGGER.debug("Retrieved {} distributions from the database.", distributions.size());

		// build the mappings and operations maps
		Map<String, Mapping> mappings = db.getMappingList().stream()
				.collect(Collectors.toMap(
						Mapping::getInstanceId,
						Function.identity(),
						(existing, replacement) -> existing));
		Map<String, Operation> operations = db.getOperationList().stream()
				.collect(Collectors.toMap(
						Operation::getInstanceId,
						Function.identity(),
						(existing, replacement) -> existing));

		// process each distribution assuming a single operation per distribution
		Set<DiscoveryItem> discoveryItems = distributions.stream()
				.filter(distribution -> isDistributionValid(distribution, operations, mappings, params))
				.map(distribution -> createDiscoveryItem(distribution))
				.collect(Collectors.toSet());
		LOGGER.info("Generated {} discovery items.", discoveryItems.size());
		return discoveryItems;
	}

	// returns true if the distribution contains all the mappings in the params
	private static boolean isDistributionValid(Distribution distribution, Map<String, Operation> operations,
			Map<String, Mapping> mappings, Map<String, String> params) {

		var supportedOps = distribution.getSupportedOperation();
		if (supportedOps == null || supportedOps.isEmpty()) {
			LOGGER.debug("Distribution {} skipped: no supported operations.", distribution.getInstanceId());
			return false;
		}
		// assume only one operation per distribution
		var operation = operations.get(supportedOps.get(0).getInstanceId());
		if (operation == null) {
			LOGGER.debug("Distribution {} skipped: no corresponding operation found for operation id {}.",
					distribution.getInstanceId(), supportedOps.get(0).getInstanceId());
			return false;
		}
		if (operation.getMapping() == null) {
			LOGGER.debug("Distribution {} skipped: operation {} has no mappings.",
					distribution.getInstanceId(), operation.getInstanceId());
			return false;
		}
		// compute mapping properties for the operation
		Set<String> mappingProps = operation.getMapping().stream()
				.map(linkedMapping -> mappings.get(linkedMapping.getInstanceId()))
				.filter(mapping -> {
					// skip null mappings
					if (mapping == null)
						return false;

					// get the value for this mapping from the parameters
					String paramValue = params.getOrDefault(mapping.getProperty(), "");

					// readonly is true but the default value is the same used by the param
					if (Boolean.parseBoolean(mapping.getReadOnlyValue()))
						return mapping.getDefaultValue() != null && mapping.getDefaultValue().equals(paramValue);

					// here is not readonly

					// whether or not this mapping is an enum
					boolean isEnum = mapping.getParamValue() != null && !mapping.getParamValue().isEmpty();

					// if it's not an enum, it's valid
					if (!isEnum) {
						return true;
					}

					// if this mapping is an enum check that the value is valid
					return mapping.getParamValue().contains(paramValue);
				})
				.map(Mapping::getProperty)
				.collect(Collectors.toSet());

		boolean valid = mappingProps.containsAll(params.keySet());
		if (!valid) {
			LOGGER.debug("Distribution {} skipped: mapping properties {} do not contain all required params {}.",
					distribution.getInstanceId(), mappingProps, params);
		}
		return valid;
	}

	// create the DiscoveryItem
	private static DiscoveryItem createDiscoveryItem(Distribution distribution) {
		LOGGER.debug("Creating discovery item for distribution {}", distribution.getInstanceId());
		List<AvailableFormat> availableFormats = AvailableFormatsGeneration.generate(distribution);

		DiscoveryItem item = new DiscoveryItemBuilder(
				distribution.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId() + "?extended=true")
				.uid(distribution.getUid())
				.metaId(distribution.getMetaId())
				.title(distribution.getTitle() != null ? String.join(";", distribution.getTitle()) : null)
				.description(
						distribution.getDescription() != null ? String.join(";", distribution.getDescription()) : null)
				.availableFormats(availableFormats)
				.versioningStatus(distribution.getStatus().name())
				.build();

		if (EnvironmentVariables.MONITORING != null && EnvironmentVariables.MONITORING.equals("true")) {
			var zabbix = ZabbixExecutor.getInstance();
			item.setStatus(zabbix.getStatusInfoFromSha(item.getSha256id()));
			item.setStatusTimestamp(zabbix.getStatusTimestampInfoFromSha(item.getSha256id()));
		}

		return item;
	}
}
