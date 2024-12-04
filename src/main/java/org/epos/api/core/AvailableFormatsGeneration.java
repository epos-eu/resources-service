package org.epos.api.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.*;

public class AvailableFormatsGeneration {

	private static final String API_PATH_EXECUTE  = EnvironmentVariables.API_CONTEXT+"/execute/";
	private static final String API_PATH_EXECUTE_OGC  = EnvironmentVariables.API_CONTEXT+"/ogcexecute/";
	private static final String API_FORMAT = "?format=";
	
	public static List<AvailableFormat> generate(Distribution distribution, List<SoftwareApplication> softwareApplications ) {

		List<AvailableFormat> formats = new ArrayList<>();
		/** Loop over all webservices related to the distribution **/
		if(distribution.getAccessService()!=null) {
			for (LinkedEntity webServiceLe : distribution.getAccessService()) {
				Optional<WebService> webService = DatabaseConnections.getInstance().getWebServiceList().parallelStream().filter(webserviceFromList -> webserviceFromList.getInstanceId().equals(webServiceLe.getInstanceId())).findFirst();
				if(webService.isPresent()) {

					boolean isWMS = false;
					boolean isWMTS = false;
					boolean isWFS = false;

					if (webService.get().getSupportedOperation() != null) {
						for (LinkedEntity supportedOperationLinkedEntity : webService.get().getSupportedOperation()) {
							Optional<Operation> operation = DatabaseConnections.getInstance().getOperationList().parallelStream().filter(operationFromList -> operationFromList.getInstanceId().equals(supportedOperationLinkedEntity.getInstanceId())).findFirst();
							if(operation.isPresent()) {
								/** Check if is an OGC service, the check at this level si based only on the template **/
								if (operation.get().getTemplate() != null) {
									if (operation.get().getTemplate().toLowerCase().contains("service=wms")) isWMS = true;
									if (operation.get().getTemplate().toLowerCase().contains("service=wmts")) isWMTS = true;
									if (operation.get().getTemplate().toLowerCase().contains("service=wfs")) isWFS = true;
								}

								if (operation.get().getMapping() != null) {
									for (LinkedEntity mapLe : operation.get().getMapping()) {
										Optional<Mapping> map = DatabaseConnections.getInstance().getMappingList().parallelStream().filter(mappingFromList -> mappingFromList.getInstanceId().equals(mapLe.getInstanceId())).findFirst();
										if(map.isPresent()) {
											/** Check if is an OGC service, the check on this level is based on a value of a variable **/
											if (map.get().getVariable()!=null && map.get().getVariable().equalsIgnoreCase("service")
													&& map.get().getParamValue() != null && (map.get().getParamValue().contains("WMS") || map.get().getParamValue().contains("wms") || map.get().getDefaultValue().equalsIgnoreCase("wms")))
												isWMS = true;
											if (map.get().getVariable()!=null && map.get().getVariable().equalsIgnoreCase("service")
													&& map.get().getParamValue() != null && (map.get().getParamValue().contains("WMTS") || map.get().getParamValue().contains("wmts") || map.get().getDefaultValue().equalsIgnoreCase("wmts")))
												isWMTS = true;
											if (map.get().getVariable()!=null && map.get().getVariable().equalsIgnoreCase("service")
													&& map.get().getParamValue() != null && (map.get().getParamValue().contains("WFS") || map.get().getParamValue().contains("wfs") || map.get().getDefaultValue().equalsIgnoreCase("wfs")))
												isWFS = true;

											/** Check if parameter has the property "encodingFormat", if yes generate an encoding format for each value **/
											if (map.get().getProperty() != null && map.get().getProperty().contains("encodingFormat")) {
												for (String pv : map.get().getParamValue()) {
													if (pv.startsWith("image/") && isWMS) {
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format("application/vnd.ogc.wms_xml")
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_OGC + distribution.getInstanceId())
																.label("WMS".toUpperCase())
																.description(AvailableFormatType.ORIGINAL)
																.build());
													} else if (pv.startsWith("image/") && isWMTS) {
														System.out.println("HELLO" + distribution.getUid());
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format("application/vnd.ogc.wmts_xml")
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_OGC + distribution.getInstanceId())
																.label("WMTS".toUpperCase())
																.description(AvailableFormatType.ORIGINAL)
																.build());
													} else if (pv.equals("json") && isWFS) {
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format("application/epos.geo+json")
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + "json")
																.label("GEOJSON (" + pv + ")")
																.description(AvailableFormatType.ORIGINAL)
																.build());
													} else if (pv.contains("geo%2Bjson")) {
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format("application/epos.geo+json")
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + pv)
																.label("GEOJSON (" + pv + ")")
																.description(AvailableFormatType.ORIGINAL)
																.build());
													} else if (
															((pv.toLowerCase().contains("geojson") || pv.toLowerCase().contains("geo+json") || pv.toLowerCase().contains("geo-json"))
																	|| (pv.toLowerCase().contains("epos.table.geo+json") || pv.toLowerCase().contains("epos.map.geo+json")))
													) {
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format("application/epos.geo+json")
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + pv)
																.label("GEOJSON (" + pv + ")")
																.description(AvailableFormatType.ORIGINAL)
																.build());
													} else {
														formats.add(new AvailableFormat.AvailableFormatBuilder()
																.originalFormat(pv)
																//.method(operation.get().getMethod())
																.format(pv)
																.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + pv)
																.label(pv.toUpperCase())
																.description(AvailableFormatType.ORIGINAL)
																.build());
													}
												}

											}
											getReturnFormats(distribution, softwareApplications, formats, operation.get());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return getAvailableFormats(distribution, formats);
	}

	private static void getReturnFormats(Distribution distribution, List<SoftwareApplication> softwareApplications, List<AvailableFormat> formats, Operation operation) {
		if (operation.getReturns() != null && formats.isEmpty()) {
			for (String returns : operation.getReturns()) {
				if (returns.contains("geojson") || returns.contains("geo+json")) {
					formats.add(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat(returns)
							.method(operation.getMethod())
							.format("application/epos.geo+json")
							.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + returns)
							.label("GEOJSON")
							.description(AvailableFormatType.ORIGINAL)
							.build());
				} else {
					formats.add(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat(returns)
							.method(operation.getMethod())
							.format(returns)
							.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + returns)
							.label(returns.toUpperCase())
							.description(AvailableFormatType.ORIGINAL)
							.build());
				}
			}
		}
		for (SoftwareApplication softwareApplication : softwareApplications) {
			if (softwareApplication.getRelation()!=null && softwareApplication.getRelation().parallelStream().map(LinkedEntity::getInstanceId).collect(Collectors.toList()).contains(operation.getInstanceId())) {
				if (softwareApplication.getParameter() != null) {
					for (LinkedEntity parameterLinkedEntity : softwareApplication.getParameter()) {
						SoftwareApplicationParameter parameter = (SoftwareApplicationParameter) LinkedEntityAPI.retrieveFromLinkedEntity(parameterLinkedEntity);
						if (parameter.getEncodingformat().equals("application/epos.geo+json")
								|| parameter.getEncodingformat().equals("application/epos.table.geo+json")
								|| parameter.getEncodingformat().equals("application/epos.map.geo+json")) {

							formats.add(new AvailableFormat.AvailableFormatBuilder()
									.originalFormat(parameter.getEncodingformat())
									.method(operation.getMethod())
									.format(parameter.getEncodingformat())
									.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + parameter.getEncodingformat())
									.label("GEOJSON")
									.description(AvailableFormatType.CONVERTED)
									.build());
						}
						if (parameter.getEncodingformat().equals("covjson")) {
							formats.add(new AvailableFormat.AvailableFormatBuilder()
									.originalFormat(parameter.getEncodingformat())
									.method(operation.getMethod())
									.format(parameter.getEncodingformat())
									.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getInstanceId() + API_FORMAT + parameter.getEncodingformat())
									.label("COVJSON")
									.description(AvailableFormatType.CONVERTED)
									.build());
						}
					}
				}
			}
		}
	}

	private static List<AvailableFormat> getAvailableFormats(Distribution distribution, List<AvailableFormat> formats) {
		if(distribution.getDownloadURL()!=null && distribution.getAccessService() ==null && !distribution.getDownloadURL().isEmpty() && distribution.getFormat()!=null) {
			String[] uri = distribution.getFormat().split("/");
			String format = uri[uri.length-1];
			formats.add(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat(format)
					.format(format)
					.href(String.join(",", distribution.getDownloadURL()))
					.label(format.toUpperCase())
					.description(AvailableFormatType.ORIGINAL)
					.build());
		}
		return formats;
	}


}
