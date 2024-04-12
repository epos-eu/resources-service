package org.epos.api.core.distributions;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableContactPoints.AvailableContactPointsBuilder;
import org.epos.api.beans.DataProduct;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.DistributionExtended;
import org.epos.api.beans.Operation;
import org.epos.api.beans.ServiceParameter;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.beans.TemporalCoverage;
import org.epos.api.beans.Webservice;
import org.epos.api.enums.ProviderType;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.dbapi.util.EDMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class DistributionDetailsExtendedGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionDetailsExtendedGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/resources/details/";
	private static final String EMAIL_SENDER = EnvironmentVariables.API_CONTEXT + "/sender/send-email?id=";

	public static DistributionExtended generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		EntityManager em = new DBService().getEntityManager();

		List<EDMDistribution> distributionSelectedList = getFromDB(em, EDMDistribution.class,
				"distribution.findAllByMetaId",
				"METAID", parameters.get("id"));

		if (distributionSelectedList.stream().noneMatch(distrSelected -> distrSelected.getState().equals("PUBLISHED")))
			return null;

		List<String> operationsIdRelatedToDistribution = null;


		EDMDistribution distributionSelected = distributionSelectedList.stream().filter(distrSelected -> distrSelected.getState().equals("PUBLISHED")).collect(Collectors.toList()).get(0);

		if (distributionSelected == null) return null;

		EDMDataproduct dp;
		if (distributionSelected.getIsDistributionsByInstanceId() != null &&
				!distributionSelected.getIsDistributionsByInstanceId().isEmpty()) {
			dp = distributionSelected.getIsDistributionsByInstanceId().stream()
					.map(EDMIsDistribution::getDataproductByInstanceDataproductId)
					.filter(edmDataproduct -> edmDataproduct.getState().equals(State.PUBLISHED.toString()))
					.findFirst().orElse(null);
			if (dp == null) return null;
		} else {
			return null;
		}

		EDMWebservice ws = distributionSelected.getWebserviceByAccessService() != null && distributionSelected.getWebserviceByAccessService().getState().equals(State.PUBLISHED.toString()) ?
				distributionSelected.getWebserviceByAccessService() : null;
		if (ws == null && distributionSelected.getAccessService() != null) return null;


		DistributionExtended distribution = new DistributionExtended();

		/**
		 * 
		 * Distribution bean population
		 * 
		 */
		if (distributionSelected.getAccessURLByInstanceId() != null) {
			operationsIdRelatedToDistribution = distributionSelected.getAccessURLByInstanceId().stream()
					.map(EDMDistributionAccessURL::getOperationByInstanceOperationId).map(EDMOperation::getInstanceId).collect(Collectors.toList());
		}

		if (distributionSelected.getDistributionTitlesByInstanceId() != null) {
			distribution.setTitle(
					Optional.of(
							distributionSelected.getDistributionTitlesByInstanceId().stream()
							.map(EDMDistributionTitle::getTitle).collect(Collectors.joining("."))
							).orElse(null)
					);
		}

		if (distributionSelected.getType() != null) {
			String[] type = distributionSelected.getType().split("\\/");
			distribution.setType(type[type.length - 1]);
		}

		if (distributionSelected.getDistributionDescriptionsByInstanceId() != null) {
			distribution.setDescription(
					Optional.of(
							distributionSelected.getDistributionDescriptionsByInstanceId().stream()
							.map(EDMDistributionDescription::getDescription).collect(Collectors.joining("."))
							).orElse(null)
					);
		}

		distribution.setId(Optional.ofNullable(distributionSelected.getMetaId()).orElse(null));
		distribution.setUid(Optional.ofNullable(distributionSelected.getUid()).orElse(null));	

		if (distributionSelected.getDistributionDownloadurlsByInstanceId() != null) {
			distribution.setDownloadURL(
					Optional.of(
							distributionSelected.getDistributionDownloadurlsByInstanceId().stream()
							.map(EDMDistributionDownloadurl::getDownloadurl).collect(Collectors.joining("."))
							).orElse(null)
					);
		}

		distribution.setLicense(Optional.ofNullable(distributionSelected.getLicense()).orElse(null));

		distribution.setFrequencyUpdate(Optional.ofNullable(dp.getAccrualperiodicity()).orElse(null));
		distribution.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getMetaId());

		Set<String> keywords = new HashSet<>(Arrays.stream(Optional.ofNullable(dp.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
		if (ws != null)
			keywords.addAll(Arrays.stream(Optional.ofNullable(ws.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
		keywords.removeAll(Collections.singleton(null));
		keywords.removeAll(Collections.singleton(""));
		distribution.setKeywords(new ArrayList<>(keywords));

		/**
		 * 
		 * DataProduct bean population
		 * 
		 */

		DataProduct dataproduct = new DataProduct();

		dataproduct.setType(dp.getType());
		dataproduct.setVersion(dp.getVersioninfo());
		dataproduct.setUid(dp.getUid());
		dataproduct.setId(dp.getInstanceId());

		if(dp.getDataproductIdentifiersByInstanceId()==null || dp.getDataproductIdentifiersByInstanceId().isEmpty()) {
			HashMap<String,String> singleIdentifier = new HashMap<String,String>();
			singleIdentifier.put("type","plain");
			singleIdentifier.put("value",dp.getUid());
			dataproduct.getIdentifiers().add(singleIdentifier);
		}else {
			dp.getDataproductIdentifiersByInstanceId().forEach(identifier -> {
				HashMap<String,String> singleIdentifier = new HashMap<String,String>();
				singleIdentifier.put("type",identifier.getType());
				singleIdentifier.put("value",identifier.getIdentifier());
				dataproduct.getIdentifiers().add(singleIdentifier);
			});
		}

		if (dp.getDataproductSpatialsByInstanceId() != null) {
			for (EDMDataproductSpatial s : dp.getDataproductSpatialsByInstanceId())
				dataproduct.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
		}


		TemporalCoverage tc = new TemporalCoverage();
		if (dp.getDataproductTemporalsByInstanceId() != null && dp.getDataproductTemporalsByInstanceId().size() > 0) {

			for(EDMDataproductTemporal temporal : dp.getDataproductTemporalsByInstanceId()) {
				Timestamp startdate = temporal.getStartdate();
				Timestamp enddate = temporal.getEnddate();
				String startDate;
				String endDate;

				if (startdate != null) {
					startDate = startdate.toString().replace(".0", "Z").replace(" ", "T");
					if (!startDate.contains("Z")) startDate = startDate + "Z";
				} else startDate = null;
				if (enddate != null) {
					endDate = enddate.toString().replace(".0", "Z").replace(" ", "T");
					if (!endDate.contains("Z")) endDate = endDate + "Z";
				} else endDate = null;

				tc.setStartDate(startDate);
				tc.setEndDate(endDate);
				dataproduct.getTemporalCoverage().add(tc);
			}
		}

		if (dp.getPublishersByInstanceId() != null) {
			List<DataServiceProvider> dataProviders = DataServiceProviderGeneration.getProviders(dp.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList()));
			dataproduct.setDataProvider(dataProviders);
		}

		if(!dp.getContactpointDataproductsByInstanceId().isEmpty()) {
			for(EDMContactpointDataproduct contacts : dp.getContactpointDataproductsByInstanceId()) {
				HashMap<String,Object> contact = new HashMap<String, Object>();
				EDMContactpoint contactpoint = contacts.getContactpointByInstanceContactpointId();
				contact.put("id", contactpoint.getInstanceId());
				contact.put("metaid", contactpoint.getMetaId());
				contact.put("uid", contactpoint.getUid());
				if(contactpoint.getEdmEntityIdByMetaPersonId()!=null && contactpoint.getEdmEntityIdByMetaPersonId().getPeopleByMetaId()!=null) {
					contactpoint.getEdmEntityIdByMetaPersonId().getPeopleByMetaId().forEach(person->{
						if(person.getState().equals("PUBLISHED")) {
							HashMap<String,Object> relatedPerson = new HashMap<String, Object>();
							relatedPerson.put("id",person.getInstanceId());
							relatedPerson.put("metaid",person.getMetaId());
							relatedPerson.put("uid",person.getUid());
							contact.put("person", relatedPerson);
						}
					});
				}
				dataproduct.getContactPoints().add(contact);
			}
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + dp.getInstanceId()+"&contactType="+ProviderType.DATAPROVIDERS)
					.type(ProviderType.DATAPROVIDERS).build());
		}


		if (dp.getDataproductCategoriesByInstanceId() != null) {
			dataproduct.setScienceDomain(Optional.of(dp.getDataproductCategoriesByInstanceId().stream()
					.map(EDMDataproductCategory::getCategoryByCategoryId)
					.map(EDMCategory::getName).collect(Collectors.toList())).orElse(null));
		}

		dataproduct.setHasQualityAnnotation(Optional.ofNullable(dp.getHasQualityAnnotation()).orElse(null));
		dataproduct.setAccessRights(Optional.ofNullable(dp.getAccessright()).orElse(null));
		dp.getDataproductProvenancesByInstanceId().forEach(instance ->{
			HashMap<String,String> prov = new HashMap<String, String>();
			prov.put("id", instance.getId());
			prov.put("provenance", instance.getProvenance());
			dataproduct.getProvenance().add(prov);
		});

		distribution.getRelatedDataProducts().add(dataproduct);

		/**
		 * 
		 * WebService bean population
		 * 
		 */
		if (ws != null) {
			Webservice webservice = new Webservice();

			for(EDMContactpointWebservice contacts : ws.getContactpointWebservicesByInstanceId()) {
				HashMap<String,Object> contact = new HashMap<String, Object>();
				EDMContactpoint contactpoint = contacts.getContactpointByInstanceContactpointId();
				contact.put("id", contactpoint.getInstanceId());
				contact.put("metaid", contactpoint.getMetaId());
				contact.put("uid", contactpoint.getUid());
				if(contactpoint.getEdmEntityIdByMetaPersonId()!=null && contactpoint.getEdmEntityIdByMetaPersonId().getPeopleByMetaId()!=null) {
					contactpoint.getEdmEntityIdByMetaPersonId().getPeopleByMetaId().forEach(person->{
						if(person.getState().equals("PUBLISHED")) {
							HashMap<String,String> relatedPerson = new HashMap<String, String>();
							relatedPerson.put("id",person.getInstanceId());
							relatedPerson.put("metaid",person.getMetaId());
							relatedPerson.put("uid",person.getUid());
							contact.put("person", relatedPerson);
						}
					});
				}
				webservice.getContactPoints().add(contact);
			}

			webservice.setDescription(Optional.ofNullable(ws.getDescription()).orElse(null));

			if (ws.getWebserviceDocumentationsByInstanceId() != null) {
				webservice.setDocumentation(Optional.of(ws.getWebserviceDocumentationsByInstanceId().stream()
						.map(EDMWebserviceDocumentation::getDocumentation)
						.collect(Collectors.joining("."))).orElse(null));
			}

			webservice.setName(Optional.ofNullable(ws.getName()).orElse(null));

			if (ws.getEdmEntityIdByProvider() != null) {
				List<DataServiceProvider> serviceProviders = DataServiceProviderGeneration.getProviders(List.of(ws.getEdmEntityIdByProvider()));
				if (!serviceProviders.isEmpty()){
					webservice.setProvider(serviceProviders.get(0));
				}
			}

			if (ws.getWebserviceSpatialsByInstanceId() != null) {
				for (EDMWebserviceSpatial s : ws.getWebserviceSpatialsByInstanceId())
					webservice.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
			}

			if (ws.getWebserviceTemporalsByInstanceId() != null && ws.getWebserviceTemporalsByInstanceId().size() > 0) {
				for(EDMWebserviceTemporal temporal : ws.getWebserviceTemporalsByInstanceId()) {
					TemporalCoverage tcws = new TemporalCoverage();
					Timestamp startdate = temporal.getStartdate();
					Timestamp enddate = temporal.getEnddate();

					String startDate;
					String endDate;

					if (startdate != null) {
						startDate = startdate.toString().replace(".0", "Z").replace(" ", "T");
						if (!startDate.contains("Z")) startDate = startDate + "Z";
					} else startDate = null;
					if (enddate != null) {
						endDate = enddate.toString().replace(".0", "Z").replace(" ", "T");
						if (!endDate.contains("Z")) endDate = endDate + "Z";
					} else endDate = null;

					tcws.setStartDate(startDate);
					tcws.setEndDate(endDate);
					webservice.getTemporalCoverage().add(tcws);
				}
			}
			if (ws.getWebserviceCategoriesByInstanceId() != null) {
				webservice.setType(Optional.of(ws.getWebserviceCategoriesByInstanceId().stream()
						.map(EDMWebserviceCategory::getCategoryByCategoryId)
						.map(EDMCategory::getName).collect(Collectors.toList())).orElse(null));
			}

			/**
			 * 
			 * WebService Operations
			 * 
			 */
			List<EDMSupportedOperation> supportedoperations = ws.getSupportedOperationByInstanceId().stream().collect(Collectors.toList());

			List<EDMOperation> operations = new ArrayList<EDMOperation>();

			if(supportedoperations!=null && supportedoperations.size()>0) {
				supportedoperations.forEach(so -> operations.add(so.getOperationByInstanceOperationId()));
			}

			// OPERATION AND PARAMETERS
			if (operations!=null && operations.size()>0) {
				for(EDMOperation op : operations) {
					if(operationsIdRelatedToDistribution!=null && operationsIdRelatedToDistribution.contains(op.getInstanceId())){
						Operation operation = new Operation();

						operation.setMethod(op.getMethod());
						operation.setEndpoint(op.getTemplate());
						operation.setUid(op.getUid());

						if (op.getMappingsByInstanceId() != null) {
							for (EDMMapping mp : op.getMappingsByInstanceId()) {
								ServiceParameter sp = new ServiceParameter();
								sp.setDefaultValue(mp.getDefaultvalue());
								sp.setEnumValue(
										mp.getMappingParamvaluesById() != null ?
												mp.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList())
												: new ArrayList<>()
										);
								sp.setName(mp.getVariable());
								sp.setMaxValue(mp.getMaxvalue());
								sp.setMinValue(mp.getMinvalue());
								sp.setLabel(mp.getLabel() != null ? mp.getLabel().replaceAll("@en", "") : null);
								sp.setProperty(mp.getProperty());
								sp.setRequired(mp.getRequired());
								sp.setType(mp.getRange() != null ? mp.getRange().replace("xsd:", "") : null);
								sp.setValue(null);
								sp.setValuePattern(mp.getValuepattern());
								sp.setVersion(null);
								sp.setReadOnlyValue(mp.getReadOnlyValue());
								sp.setMultipleValue(mp.getMultipleValues());
								operation.getServiceParameters().add(sp);
							}
						}
						webservice.getOperations().add(operation);
					}
				}
			}
			distribution.getRelatedWebservice().add(webservice);
		}

		/**
		 * 
		 * ContactPoints bean population
		 * 
		 */

		if(ws!=null && !ws.getContactpointWebservicesByInstanceId().isEmpty()) {
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + ws.getInstanceId()+"&contactType="+ProviderType.SERVICEPROVIDERS)
					.type(ProviderType.SERVICEPROVIDERS).build());
		}


		if(!dp.getContactpointDataproductsByInstanceId().isEmpty()) {
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + dp.getInstanceId()+"&contactType="+ProviderType.DATAPROVIDERS)
					.type(ProviderType.DATAPROVIDERS).build());
		}

		if((ws!=null && !ws.getContactpointWebservicesByInstanceId().isEmpty() && !dp.getContactpointDataproductsByInstanceId().isEmpty())){
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + distribution.getId()+"&contactType="+ProviderType.ALL)
					.type(ProviderType.ALL).build());
		}


		distribution.setAvailableFormats(AvailableFormatsGeneration.generate(distributionSelected));

		// TEMP SECTION
		ArrayList<DiscoveryItem> discoveryList = new ArrayList<>();

		Set<String> facetsDataProviders = new HashSet<String>();
		if(dp.getPublishersByInstanceId() != null ) {
			for (EDMEdmEntityId edmMetaId : dp.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())) {
				if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
					ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
					list.sort(EDMUtil::compareEntityVersion);
					EDMOrganization edmDataproductRelatedOrganization = list.get(0);
					if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
						facetsDataProviders.add(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
					}
				}
			}
		}

		Set<String> facetsServiceProviders = new HashSet<String>();
		if(distributionSelected.getWebserviceByAccessService() != null && distributionSelected.getWebserviceByAccessService().getEdmEntityIdByProvider() != null) {
			EDMEdmEntityId edmMetaId = distributionSelected.getWebserviceByAccessService().getEdmEntityIdByProvider();
			if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
				ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
				list.sort(EDMUtil::compareEntityVersion);
				EDMOrganization edmWebserviceRelatedOrganization = list.get(0);
				if(edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
					facetsServiceProviders.add(edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
				}
			}
		}
		List<String> categoryList = dp.getDataproductCategoriesByInstanceId().stream()
				.map(EDMDataproductCategory::getCategoryByCategoryId)
				.filter(Objects::nonNull)
				.filter(e->e.getUid().contains("category:"))
				.map(EDMCategory::getUid)
				.collect(Collectors.toList());

		discoveryList.add(new DiscoveryItem.DiscoveryItemBuilder(distributionSelected.getMetaId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getMetaId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getMetaId()+"?extended=true")
				.uid(distribution.getUid())
				.title(distribution.getTitle())
				.description(distribution.getDescription())
				.availableFormats(AvailableFormatsGeneration.generate(distributionSelected))
				.setSha256id(DigestUtils.sha256Hex(distribution.getUid()))
				.setDataprovider(facetsDataProviders)
				.setServiceProvider(facetsServiceProviders)
				.setCategories(categoryList.isEmpty()? null : categoryList)
				.build());

		FacetsNodeTree categories = FacetsGeneration.generateResponseUsingCategories(discoveryList);
		categories.getNodes().forEach(node -> node.setDistributions(null));
		distribution.setCategories(categories.getFacets());

		em.close();

		return distribution;
	}

}