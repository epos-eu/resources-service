package org.epos.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.AvailableFormatConverted;
import org.epos.api.enums.AvailableFormatType;
import org.epos.eposdatamodel.Parameter;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;

public class AvailableFormatsGeneration {

	private static final String API_PATH_EXECUTE  = EnvironmentVariables.API_CONTEXT+"/execute/";
	private static final String API_PATH_EXECUTE_OGC  = EnvironmentVariables.API_CONTEXT+"/ogcexecute/";
	private static final String API_FORMAT = "?format=";
	
	public static List<AvailableFormat> generate(EDMDistribution distribution) {

		List<AvailableFormat> formats = new ArrayList<>();
		
		boolean isogcformat = false;
		//available formats
		EDMWebservice webserviceByAccessService = distribution.getWebserviceByAccessService();
		if(webserviceByAccessService !=null && webserviceByAccessService.getSupportedOperationByInstanceId() != null) {
			for( EDMOperation op : webserviceByAccessService.getSupportedOperationByInstanceId().stream().map(EDMSupportedOperation::getOperationByInstanceOperationId).collect(Collectors.toList())){
				if (op.getUid() != null && distribution.getAccessURLByInstanceId() != null && distribution.getAccessURLByInstanceId().stream().map(EDMDistributionAccessURL::getInstanceOperationId).collect(Collectors.toList()).contains(op.getInstanceId())) {
					for (EDMMapping map : op.getMappingsByInstanceId() != null ? op.getMappingsByInstanceId() : new ArrayList<EDMMapping>()) {
						if (map.getProperty() != null && map.getProperty().contains("encodingFormat")) {
							for (String pv : map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList())) {
								if (pv.startsWith("image/") && (op.getTemplate().toLowerCase().contains("service=wms") || 
										op.getMappingsByInstanceId().stream()
										.anyMatch(e -> e.getVariable().toLowerCase().equals("service") && (map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList()).contains("WMS") || e.getDefaultvalue().toLowerCase().contains("wms"))))) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/vnd.ogc.wms_xml")
											.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_OGC + distribution.getMetaId())
											.label("WMS".toUpperCase())
											.description(AvailableFormatType.ORIGINAL)
											.build());
									isogcformat = true;
								} else if (pv.startsWith("image/") && (op.getTemplate().toLowerCase().contains("service=wmts") || 
										op.getMappingsByInstanceId().stream()
										.anyMatch(e -> e.getVariable().toLowerCase().equals("service") && (map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList()).contains("WMTS") || e.getDefaultvalue().toLowerCase().contains("wmts"))))) {
									System.out.println("HELLO" + distribution.getUid());
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/vnd.ogc.wmts_xml")
											.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_OGC + distribution.getMetaId())
											.label("WMTS".toUpperCase())
											.description(AvailableFormatType.ORIGINAL)
											.build());
									isogcformat = true;
								} else if (pv.equals("json") &&
										(op.getTemplate().toLowerCase().contains("service=wfs") ||
												op.getMappingsByInstanceId().stream()
												.anyMatch(e -> e.getVariable().toLowerCase().equals("service") && (map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList()).contains("WFS") || e.getDefaultvalue().toLowerCase().contains("wfs"))))) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + "json")
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else if (pv.contains("geo%2Bjson")) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else if (
										((pv.toLowerCase().contains("geojson") || pv.toLowerCase().contains("geo+json") || pv.toLowerCase().contains("geo-json")) 
												|| (pv.toLowerCase().contains("epos.table.geo+json") || pv.toLowerCase().contains("epos.map.geo+json")))
										&& op.getSoftwareapplicationOperationsByInstanceId().stream().map(EDMSoftwareapplicationOperation::getSoftwareapplicationByInstanceSoftwareapplicationId).collect(Collectors.toList()).isEmpty()
										) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format(pv)
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label(pv.toUpperCase())
											.description(AvailableFormatType.ORIGINAL)
											.build());
								}
							}
						}
					}
					if (op.getOperationReturnsByInstanceId() != null && formats.isEmpty()) {
						for (String returns : op.getOperationReturnsByInstanceId().stream().map(EDMOperationReturns::getReturns).collect(Collectors.toList())) {
							if (returns.contains("geojson") || returns.contains("geo+json")) {
								formats.add(new AvailableFormat.AvailableFormatBuilder()
										.originalFormat(returns)
										.format("application/epos.geo+json")
										.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
										.label("GEOJSON")
										.description(AvailableFormatType.ORIGINAL)
										.build());
							} else {
								formats.add(new AvailableFormat.AvailableFormatBuilder()
										.originalFormat(returns)
										.format(returns)
										.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
										.label(returns.toUpperCase())
										.description(AvailableFormatType.ORIGINAL)
										.build());
							}
						}
					}
					if (op != null && !isogcformat) {
						for(Object[] item : getAllPluginsIdFromSoftwareApplicationId(op.getInstanceId())) {
							try {
								if (item[2].toString().equals("application/epos.geo+json")
										|| item[2].toString().equals("application/epos.table.geo+json")
										|| item[2].toString().equals("application/epos.map.geo+json")) {

									formats.add(new AvailableFormatConverted.AvailableFormatConvertedBuilder()
											.inputFormat(item[1].toString())
											.pluginId(item[0].toString())
											.originalFormat(item[1].toString())
											.format(item[2].toString())
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + item[2].toString())
											.label("GEOJSON")
											.description(AvailableFormatType.CONVERTED)
											.build());
								} else if (item[2].toString().equals("application/epos.graph.covjson") || item[2].toString().equals("application/epos.covjson")) {
									formats.add(new AvailableFormatConverted.AvailableFormatConvertedBuilder()
											.inputFormat(item[1].toString())
											.pluginId(item[0].toString())
											.originalFormat(item[1].toString())
											.format(item[2].toString())
										.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + item[2].toString())
											.label("COVJSON")
											.description(AvailableFormatType.CONVERTED)
											.build());
								} else {
									System.out.println("Unknown format: " + item[2].toString());
								}
							} catch (Exception e) {
								// If there is an error while creating a format object just skip it
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		if(distribution.getDistributionDownloadurlsByInstanceId()!=null && webserviceByAccessService ==null && distribution.getDistributionDownloadurlsByInstanceId().size() > 0 && distribution.getFormat()!=null) {
			String[] uri = distribution.getFormat().split("\\/");
			String format = uri[uri.length-1];
			formats.add(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat(format)
					.format(format)
					.href(distribution.getDistributionDownloadurlsByInstanceId().stream()
							.map(EDMDistributionDownloadurl::getDownloadurl).findFirst().get())
					.label(format.toUpperCase())
					.description(AvailableFormatType.ORIGINAL)
					.build());
		}
		return formats;
	}

	private static List<Object[]> getAllPluginsIdFromSoftwareApplicationId(String operationId) {
		EntityManager em = new DBService().getEntityManager();
		List<Object[]> resultList = em.createNativeQuery("select plugin.id, plugin_relations.input_format, plugin_relations.output_format from plugin_relations join plugin on plugin_relations.plugin_id = plugin.id where plugin_relations.relation_id = '" + operationId + "' and plugin.enabled = true").getResultList();
		return resultList;
	}
}

