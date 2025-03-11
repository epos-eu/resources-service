package org.epos.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.AvailableFormatConverted;
import org.epos.api.beans.Plugin;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Mapping;
import org.epos.eposdatamodel.Operation;

public class AvailableFormatsGeneration {

	private static final String API_PATH_EXECUTE = EnvironmentVariables.API_CONTEXT + "/execute/";
	private static final String API_PATH_EXECUTE_OGC = EnvironmentVariables.API_CONTEXT + "/ogcexecute/";
	private static final String API_FORMAT = "?format=";

	// Helper method to avoid repetitive creation of AvailableFormat objects
	private static AvailableFormat buildAvailableFormat(String originalFormat, String format, String href, String label,
			AvailableFormatType type) {
		return new AvailableFormat.AvailableFormatBuilder()
				.originalFormat(originalFormat)
				.format(format)
				.href(href)
				.label(label)
				.type(type)
				.build();
	}

	private static AvailableFormat buildAvailableFormatConverted(String inputFormat, String pluginId,
			String originalFormat, String format, String href, String label, AvailableFormatType type) {
		return new AvailableFormatConverted.AvailableFormatConvertedBuilder()
				.inputFormat(inputFormat)
				.pluginId(pluginId)
				.originalFormat(originalFormat)
				.format(format)
				.href(href)
				.label(label)
				.type(type)
				.build();
	}

	public static List<AvailableFormat> generate(Distribution distribution) {
		List<AvailableFormat> formats = new ArrayList<>();

		// DOWNLOADABLE FILE
		if (distribution.getDownloadURL() != null && distribution.getAccessService() == null
				&& !distribution.getDownloadURL().isEmpty() && distribution.getFormat() != null) {
			String[] uri = distribution.getFormat().split("/");
			String format = uri[uri.length - 1];
			formats.add(buildAvailableFormat(format, format, String.join(",", distribution.getDownloadURL()),
					format.toUpperCase(), AvailableFormatType.ORIGINAL));
			return formats;
		}

		// If the operation for this distribution is null return empty formats
		if (distribution.getSupportedOperation() == null)
			return formats;

		// WEBSERVICE
		List<Operation> operationList = DatabaseConnections.getInstance()
				.getOperationList().stream()
				.filter(item -> distribution.getSupportedOperation().stream()
						.map(LinkedEntity::getInstanceId)
						.collect(Collectors.toList())
						.contains(item.getInstanceId()))
				.collect(Collectors.toList());
		for (Operation operation : operationList) {
			// Skip this operation if it is null
			if (operation == null)
				continue;

			boolean isOgcFormat = false;

			if (operation.getMapping() != null) {
				List<Mapping> mappings = DatabaseConnections.getInstance()
						.getMappingList().stream()
						.filter(item -> operation.getMapping().stream()
								.map(LinkedEntity::getInstanceId)
								.collect(Collectors.toList())
								.contains(item.getInstanceId()))
						.collect(Collectors.toList());
				for (Mapping map : mappings) {
					if (map != null && map.getProperty() != null && map.getProperty().contains("encodingFormat")) {
						for (String pv : map.getParamValue()) {
							// OGC Format Check
							if (pv.startsWith("image/")) {
								if (operation.getTemplate().toLowerCase().contains("service=wms")
										|| containsServiceInMappings(mappings, "WMS", map)) {
									formats.add(buildAvailableFormat(
											pv,
											"application/vnd.ogc.wms_xml",
											buildHrefOgc(distribution),
											"WMS",
											AvailableFormatType.ORIGINAL));
									isOgcFormat = true;
								} else if (operation.getTemplate().toLowerCase().contains("service=wmts")
										|| containsServiceInMappings(mappings, "WMTS", map)) {
									formats.add(buildAvailableFormat(
											pv,
											"application/vnd.ogc.wmts_xml",
											buildHrefOgc(distribution),
											"WMTS",
											AvailableFormatType.ORIGINAL));
									isOgcFormat = true;
								}
							} else if (pv.equals("json")
									&& operation.getTemplate()!=null && (operation.getTemplate().toLowerCase().contains("service=wfs")
											|| containsServiceInMappings(mappings, "WFS", map))) {
								formats.add(buildAvailableFormat(
										pv,
										"application/epos.geo+json",
										buildHref(distribution, "json"),
										"GEOJSON (" + pv + ")",
										AvailableFormatType.ORIGINAL));
							} else if (pv.contains("geo%2Bjson")
									|| pv.toLowerCase().matches(".*geo(?:json|\\+json|-json).*")) {
								formats.add(buildAvailableFormat(
										pv,
										"application/epos.geo+json",
										buildHref(distribution, pv),
										"GEOJSON (" + pv + ")",
										AvailableFormatType.ORIGINAL));
							} else {
								formats.add(buildAvailableFormat(
										pv,
										pv,
										buildHref(distribution, pv),
										pv.toUpperCase(),
										AvailableFormatType.ORIGINAL));
							}
						}
					}
				}
			}

			if (operation.getReturns() != null && formats.isEmpty()) {
				for (String returns : operation.getReturns()) {
					if (returns.contains("geojson") || returns.contains("geo+json")) {
						formats.add(buildAvailableFormat(
								returns,
								"application/epos.geo+json",
								buildHref(distribution, returns),
								"GEOJSON",
								AvailableFormatType.ORIGINAL));
					} else {
						formats.add(buildAvailableFormat(
								returns,
								returns,
								buildHref(distribution, returns),
								returns.toUpperCase(),
								AvailableFormatType.ORIGINAL));
					}
				}
			}

			if (!isOgcFormat) {
				for (Plugin plugin : DatabaseConnections.getInstance().getPlugins()) {
					if (plugin.getOperationId().equals(operation.getInstanceId())) {
						for (Plugin.Relations relation : plugin.getRelations()) {
							if (relation.getOutputFormat().equals("application/epos.geo+json")
									|| relation.getOutputFormat().equals("application/epos.table.geo+json")
									|| relation.getOutputFormat().equals("application/epos.map.geo+json")) {
								formats.add(buildAvailableFormatConverted(
										relation.getInputFormat(),
										relation.getPluginId(),
										relation.getInputFormat(),
										relation.getOutputFormat(),
										buildHref(distribution, relation.getOutputFormat()),
										"GEOJSON",
										AvailableFormatType.CONVERTED));
							} else if (relation.getOutputFormat().equals("application/epos.graph.covjson")
									|| relation.getOutputFormat().equals("application/epos.covjson")) {
								formats.add(buildAvailableFormatConverted(
										relation.getInputFormat(),
										relation.getPluginId(),
										relation.getInputFormat(),
										relation.getOutputFormat(),
										buildHref(distribution, relation.getOutputFormat()),
										"COVJSON",
										AvailableFormatType.CONVERTED));
							} else {
								System.out.println("Unknown format: " + relation.getOutputFormat());
							}
						}
					}
				}
			}
		}

		return formats;
	}

	// Helper method to check if a service exists in the mappings
	private static boolean containsServiceInMappings(List<Mapping> mappings, String service, Mapping map) {
		return mappings.stream()
				.anyMatch(e -> e.getVariable().equalsIgnoreCase("service")
						&& (map.getParamValue().contains(service)
								|| e.getDefaultValue().toLowerCase().contains(service.toLowerCase())));
	}

	// Helper method to build the href for a given distribution and format
	private static String buildHref(Distribution distribution, String format) {
		return EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + format;
	}

	private static String buildHrefOgc(Distribution distribution) {
		return EnvironmentVariables.API_HOST + API_PATH_EXECUTE_OGC + distribution.getInstanceId();
	}
}
