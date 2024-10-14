package org.epos.api.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.epos.handler.dbapi.DBAPIClient;
import org.epos.handler.dbapi.dbapiimplementation.DataProductDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.SoftwareApplicationDBAPI;
import org.epos.handler.dbapi.model.EDMDistribution;
import org.epos.handler.dbapi.model.EDMDistributionAccessURL;
import org.epos.handler.dbapi.model.EDMDistributionTitle;
import org.epos.handler.dbapi.model.EDMOperation;
import org.epos.handler.dbapi.service.DBService;
import org.epos.api.beans.Distribution;
import org.epos.api.beans.MonitoringBean;
import org.epos.api.core.distributions.DistributionDetailsGenerationJPA;
import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.ContactPoint;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Identifier;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Parameter;
import org.epos.eposdatamodel.Parameter.ActionEnum;
import org.epos.eposdatamodel.SoftwareApplication;
import org.epos.eposdatamodel.State;
import org.epos.eposdatamodel.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class MonitoringGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringGeneration.class); 

	private static final DBAPIClient dbapi = new DBAPIClient();

	public static List<MonitoringBean> generate() {
		
		EntityManager em = new DBService().getEntityManager();

		List<SoftwareApplication> softwareApplicationList = new SoftwareApplicationDBAPI().getAllByState(State.PUBLISHED);
		List<MonitoringBean> monitoringList = new ArrayList<>();
		List<DataProduct> datasetList = new DataProductDBAPI().getAllByState(State.PUBLISHED);
		List<EDMDistribution> distributionList = getFromDB(em, EDMDistribution.class, "distribution.findAllByState", "STATE", "PUBLISHED");
		
		System.out.println("Start loop");

		for(EDMDistribution dx : distributionList) {

			MonitoringBean mb = new MonitoringBean();
			//IDENTIFIER
			mb.setIdentifier(dx.getMetaId());
			
			String title = null;
			if (dx.getDistributionTitlesByInstanceId() != null && !dx.getDistributionTitlesByInstanceId().isEmpty()) {
				EDMDistributionTitle edmDistributionTitles = new ArrayList<>(dx.getDistributionTitlesByInstanceId()).get(0);
				title = edmDistributionTitles.getTitle();
			}
			
			mb.setName(title);

			Map<String,Object> params = new HashMap<String, Object>();
			params.put("id", dx.getMetaId());
			params.put("useDefaults", "true");
			
			Distribution distribution = DistributionDetailsGenerationJPA.generate(params);
			
			HashMap<String, Object> parametersMap = new HashMap<>();

			if(distribution!=null) {
				
				if(distribution.getParameters()!=null) {
					distribution.getParameters().forEach(p -> {
						if (p.getDefaultValue() != null && !p.getDefaultValue().isEmpty()) {
							if(p.getValuePattern()!=null && p.getProperty()!=null && (p.getProperty().equals("schema:startDate") || p.getProperty().equals("schema:endDate"))) {
								p.setDefaultValue(Utils.convertDateUsingPattern(p.getDefaultValue(), Utils.EPOSINTERNALFORMAT, p.getValuePattern()));
							}
							if(p.getDefaultValue().contains("%20")) {
								parametersMap.put(p.getName(), p.getDefaultValue());
							}
							else parametersMap.put(p.getName(), URLGeneration.encodeValue(p.getDefaultValue()));
						}
					});
				}
				if(distribution.getEndpoint()!=null) {
					String compiledUrl = null;
					compiledUrl = URLGeneration.generateURLFromTemplateAndMap(distribution.getEndpoint(), parametersMap);

					try {
						compiledUrl = URLGeneration.ogcWFSChecker(compiledUrl);
					}catch(Exception e) {
						LOGGER.error("Found the following issue whilst executing the WFS Checker, issue raised "+ e.getMessage() + " - Continuing execution");
					}
					
					//compiledUrl = java.net.URLDecoder.decode(compiledUrl, StandardCharsets.UTF_8);
					mb.setOriginalURL(compiledUrl);
				}

				//DDSS
				for(DataProduct d : datasetList) {
					ArrayList<String> distrs = new ArrayList<String>();
					d.getDistribution().forEach(dist->{
						distrs.add(dist.getMetaId());
					});
					if(distrs.contains(dx.getMetaId())) {
						String ddss = null;

						for (Identifier i : d.getIdentifier()) {
							if(i.getType().equals("DDSS-ID")){
								ddss = i.getIdentifier();
							}
						}
						if(ddss == null) continue;

						if(ddss.toLowerCase().contains("wp08")) mb.setTCSGroup("Seismology");
						else if(ddss.toLowerCase().contains("wp09")) mb.setTCSGroup("Near Fault Observations");
						else if(ddss.toLowerCase().contains("wp10")) mb.setTCSGroup("Geodesy");
						else if(ddss.toLowerCase().contains("wp11")) mb.setTCSGroup("Volcano Observations");
						else if(ddss.toLowerCase().contains("wp12")) mb.setTCSGroup("Satellite Observations");
						else if(ddss.toLowerCase().contains("wp13")) mb.setTCSGroup("Geoelectromagnetism");
						else if(ddss.toLowerCase().contains("wp14")) mb.setTCSGroup("Anthropogenic Hazard Observations");
						else if(ddss.toLowerCase().contains("wp15")) mb.setTCSGroup("Geology");
						else if(ddss.toLowerCase().contains("wp16")) mb.setTCSGroup("Multi-Scale Laboratory");
						else if(ddss.toLowerCase().contains("wp18")) mb.setTCSGroup("Tsunami");
						else mb.setTCSGroup("Undefined");


						if(dx.getAccessService()!=null) {
							for(WebService ws : dbapi.retrieve(WebService.class, new DBAPIClient.GetQuery().instanceId(dx.getAccessService()))) {
								for(LinkedEntity le : ws.getContactPoint()) {
									dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
									.forEach(contact->  mb.createContacts(contact.getUid(),contact.getRole(), contact.getEmail()));
								}	
							}
						}
					}
				}
				//VALIDATION RULES
				for(SoftwareApplication sw : softwareApplicationList) {
					for(String value : sw.getIdentifier().stream().map(Identifier::getIdentifier).collect(Collectors.toList())) {
						if(value.toLowerCase().contains("monitoring/zabbix")) {
							if(dx.getAccessURLByInstanceId()!=null && sw.getRelation().stream().map(LinkedEntity::getUid).collect(Collectors.toList()).containsAll(dx.getAccessURLByInstanceId().stream()
									.map(EDMDistributionAccessURL::getOperationByInstanceOperationId).map(EDMOperation::getUid).collect(Collectors.toList()))){
								String validationtype = sw.getRequirements().replace("validation-type=", "");
								if(validationtype.equals("")) validationtype="none";
								String encodingFormatObject = null;
								String schemaversionObject = null;
								for (Parameter parameter : sw.getParameter()) {
									if (parameter.getAction().equals(ActionEnum.OBJECT)){
										encodingFormatObject = parameter.getEncodingFormat();
										schemaversionObject = parameter.getConformsTo();
									}
								}
								mb.createValidationRule(validationtype, encodingFormatObject, schemaversionObject);
							}
						}
					}
				}
				if(mb.getValidationRules()==null || mb.getValidationRules().isEmpty()) {
					mb.createValidationRule("none", null, null);
				}

				mb.setId(dx.getMetaId());
				mb.setUid(dx.getUid());
				if(mb.getOriginalURL()!=null)
					monitoringList.add(mb);
			}
		}
		return monitoringList;
	}

}
