package org.epos.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.enums.AvailableFormatType;
import org.epos.eposdatamodel.*;

public class AvailableFormatsGeneration {

	private static final String API_PATH_EXECUTE  = EnvironmentVariables.API_CONTEXT+"/execute/";
	private static final String API_PATH_EXECUTE_OGC  = EnvironmentVariables.API_CONTEXT+"/ogcexecute/";
	private static final String API_FORMAT = "?format=";
	
	public static List<AvailableFormat> generate(Distribution distribution) {

		List<AvailableFormat> formats = new ArrayList<>();
		/** Loop over all webservices related to the distribution **/
		if(distribution.getAccessService()!=null) {
			for (LinkedEntity accessServiceLinkedEntity : distribution.getAccessService()) {

				boolean isWMS = false;
				boolean isWMTS = false;
				boolean isWFS = false;

				WebService webService = (WebService) LinkedEntityAPI.retrieveFromLinkedEntity(accessServiceLinkedEntity);
				if (webService != null && webService.getSupportedOperation() != null) {
					for (LinkedEntity supportedOperationLinkedEntity : webService.getSupportedOperation()) {
						Operation operation = (Operation) LinkedEntityAPI.retrieveFromLinkedEntity(supportedOperationLinkedEntity);
						/** Check if is an OGC service, the check at this level si based only on the template **/
						if (operation.getTemplate().toLowerCase().contains("service=wms")) isWMS = true;
						if (operation.getTemplate().toLowerCase().contains("service=wmts")) isWMTS = true;
						if (operation.getTemplate().toLowerCase().contains("service=wfs")) isWFS = true;

						for (LinkedEntity mappingLinkedEntity : operation.getMapping()) {
							Mapping map = (Mapping) LinkedEntityAPI.retrieveFromLinkedEntity(mappingLinkedEntity);
							/** Check if is an OGC service, the check on this level is based on a value of a variable **/
							if (map.getVariable().equalsIgnoreCase("service")
									&& (map.getParamValue().contains("WMS") || map.getParamValue().contains("wms") || map.getDefaultValue().equalsIgnoreCase("wms")))
								isWMS = true;
							if (map.getVariable().equalsIgnoreCase("service")
									&& (map.getParamValue().contains("WMTS") || map.getParamValue().contains("wmts") || map.getDefaultValue().equalsIgnoreCase("wmts")))
								isWMTS = true;
							if (map.getVariable().equalsIgnoreCase("service")
									&& (map.getParamValue().contains("WFS") || map.getParamValue().contains("wfs") || map.getDefaultValue().equalsIgnoreCase("wfs")))
								isWFS = true;

							/** Check if parameter has the property "encodingFormat", if yes generate an encoding format for each value **/
							if (map.getProperty() != null && map.getProperty().contains("encodingFormat")) {
								for (String pv : map.getParamValue()) {
									if (pv.startsWith("image/") && isWMS) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format("application/vnd.ogc.wms_xml")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_OGC + distribution.getMetaId())
												.label("WMS".toUpperCase())
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else if (pv.startsWith("image/") && isWMTS) {
										System.out.println("HELLO" + distribution.getUid());
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format("application/vnd.ogc.wmts_xml")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_OGC + distribution.getMetaId())
												.label("WMTS".toUpperCase())
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else if (pv.equals("json") && isWFS) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format("application/epos.geo+json")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + "json")
												.label("GEOJSON (" + pv + ")")
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else if (pv.contains("geo%2Bjson")) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format("application/epos.geo+json")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
												.label("GEOJSON (" + pv + ")")
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else if (
											((pv.toLowerCase().contains("geojson") || pv.toLowerCase().contains("geo+json") || pv.toLowerCase().contains("geo-json"))
													|| (pv.toLowerCase().contains("epos.table.geo+json") || pv.toLowerCase().contains("epos.map.geo+json")))
									) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format("application/epos.geo+json")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
												.label("GEOJSON (" + pv + ")")
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(pv)
												.method(operation.getMethod())
												.format(pv)
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
												.label(pv.toUpperCase())
												.description(AvailableFormatType.ORIGINAL)
												.build());
									}
								}
							}
							if (operation.getReturns() != null && formats.isEmpty()) {
								for (String returns : operation.getReturns()) {
									if (returns.contains("geojson") || returns.contains("geo+json")) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(returns)
												.method(operation.getMethod())
												.format("application/epos.geo+json")
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
												.label("GEOJSON")
												.description(AvailableFormatType.ORIGINAL)
												.build());
									} else {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(returns)
												.method(operation.getMethod())
												.format(returns)
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
												.label(returns.toUpperCase())
												.description(AvailableFormatType.ORIGINAL)
												.build());
									}
								}
							}
							List<SoftwareApplication> softwareApplications = AbstractAPI.retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll();
							for (SoftwareApplication softwareApplication : softwareApplications) {
								if (softwareApplication.getRelation().stream().map(LinkedEntity::getInstanceId).collect(Collectors.toList()).contains(operation.getInstanceId())) {
									if (softwareApplication.getParameter() != null) {
										ArrayList<Parameter> parameterList = new ArrayList<>();
										for (LinkedEntity parameterLinkedEntity : softwareApplication.getParameter()) {
											Parameter parameter = (Parameter) LinkedEntityAPI.retrieveFromLinkedEntity(parameterLinkedEntity);
											if (parameter.getEncodingFormat().equals("application/epos.geo+json")
													|| parameter.getEncodingFormat().equals("application/epos.table.geo+json")
													|| parameter.getEncodingFormat().equals("application/epos.map.geo+json")) {

												formats.add(new AvailableFormat.AvailableFormatBuilder()
														.originalFormat(parameter.getEncodingFormat())
														.method(operation.getMethod())
														.format(parameter.getEncodingFormat())
														.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + parameter.getEncodingFormat())
														.label("GEOJSON")
														.description(AvailableFormatType.CONVERTED)
														.build());
											}
											if (parameter.getEncodingFormat().equals("covjson")) {
												formats.add(new AvailableFormat.AvailableFormatBuilder()
														.originalFormat(parameter.getEncodingFormat())
														.method(operation.getMethod())
														.format(parameter.getEncodingFormat())
														.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + parameter.getEncodingFormat())
														.label("COVJSON")
														.description(AvailableFormatType.CONVERTED)
														.build());
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
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
