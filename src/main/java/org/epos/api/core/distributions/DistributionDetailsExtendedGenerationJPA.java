package org.epos.api.core.distributions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableContactPoints.AvailableContactPointsBuilder;
import org.epos.api.beans.DataProduct;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.DistributionExtended;
import org.epos.api.beans.Operation;
import org.epos.api.beans.ServiceParameter;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.beans.TemporalCoverage;
import org.epos.api.beans.Webservice;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.enums.ProviderType;
import org.epos.api.facets.Facets;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.ContactPoint;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.Documentation;
import org.epos.eposdatamodel.Identifier;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Location;
import org.epos.eposdatamodel.Mapping;
import org.epos.eposdatamodel.Organization;
import org.epos.eposdatamodel.PeriodOfTime;
import org.epos.eposdatamodel.Person;
import org.epos.eposdatamodel.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;

public class DistributionDetailsExtendedGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionDetailsExtendedGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/resources/details/";
	private static final String EMAIL_SENDER = EnvironmentVariables.API_CONTEXT + "/sender/send-email?id=";

	public static DistributionExtended generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		Distribution distributionSelected = (Distribution) AbstractAPI.retrieveAPI(EntityNames.DISTRIBUTION.name()).retrieve(parameters.get("id").toString());

		List<String> operationsIdRelatedToDistribution = null;

		if (distributionSelected == null) return null;

		org.epos.eposdatamodel.DataProduct dp;
		if (distributionSelected.getDataProduct() != null &&
				!distributionSelected.getDataProduct().isEmpty()) {
			dp = (org.epos.eposdatamodel.DataProduct) LinkedEntityAPI.retrieveFromLinkedEntity(distributionSelected.getDataProduct().get(0));
			if (dp == null) return null;
		} else {
			return null;
		}

		WebService ws = distributionSelected.getAccessService() != null && !distributionSelected.getAccessService().isEmpty() ?
			(WebService) LinkedEntityAPI.retrieveFromLinkedEntity(distributionSelected.getAccessService().get(0)) : null;
		if (ws == null && distributionSelected.getAccessService() != null) return null;

		DistributionExtended distribution = new DistributionExtended();

		/**
		 * 
		 * Distribution bean population
		 * 
		 */
		if (distributionSelected.getSupportedOperation() != null) {
			operationsIdRelatedToDistribution = distributionSelected.getSupportedOperation().stream()
					.map(LinkedEntity::getInstanceId).collect(Collectors.toList());
		}

		if (distributionSelected.getTitle() != null) {
			distribution.setTitle(
					Optional.of(
                            String.join(".", distributionSelected.getTitle())
							).orElse(null)
					);
		}

		if (distributionSelected.getType() != null) {
			String[] type = distributionSelected.getType().split("/");
			distribution.setType(type[type.length - 1]);
		}

		if (distributionSelected.getDescription() != null) {
			distribution.setDescription(
					Optional.of(
							String.join(".", distributionSelected.getDescription())
					).orElse(null)
			);
		}

		distribution.setId(distributionSelected.getInstanceId());
		distribution.setUid(distributionSelected.getUid());
		distribution.setVersioningStatus(distributionSelected.getVersion());

		if (distributionSelected.getDownloadURL() != null) {
			distribution.setDownloadURL(
					Optional.of(
							String.join(".", distributionSelected.getDownloadURL())
					).orElse(null)
			);
		}

		distribution.setLicense(distributionSelected.getLicence());
		distribution.setFrequencyUpdate(dp.getAccrualPeriodicity());
		distribution.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId());
		distribution.setHrefExtended(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId()+"?extended=true");

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
		dataproduct.setVersion(dp.getVersionInfo());
		dataproduct.setUid(dp.getUid());
		dataproduct.setId(dp.getInstanceId());

		if(dp.getIdentifier()==null || dp.getIdentifier().isEmpty()) {
			HashMap<String,String> singleIdentifier = new HashMap<String,String>();
			singleIdentifier.put("type","plain");
			singleIdentifier.put("value",dp.getUid());
			dataproduct.getIdentifiers().add(singleIdentifier);
		}else {
			dp.getIdentifier().forEach(identifierLe -> {
				Identifier identifier = (Identifier) LinkedEntityAPI.retrieveFromLinkedEntity(identifierLe);
				HashMap<String,String> singleIdentifier = new HashMap<String,String>();
				singleIdentifier.put("type",identifier.getType());
				singleIdentifier.put("value",identifier.getIdentifier());
				dataproduct.getIdentifiers().add(singleIdentifier);
			});
		}

		if (dp.getSpatialExtent() != null) {
			for (LinkedEntity le : dp.getSpatialExtent()) {
				Location s = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(le);
				dataproduct.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
			}
		}

		TemporalCoverage tc = new TemporalCoverage();
		if (dp.getTemporalExtent() != null && !dp.getTemporalExtent().isEmpty()) {
			PeriodOfTime periodOfTime = (PeriodOfTime) LinkedEntityAPI.retrieveFromLinkedEntity(dp.getTemporalExtent().get(0));
			String startDate = null;
			String endDate = null;
			if(periodOfTime.getStartDate()!=null){
				startDate = Timestamp.valueOf(periodOfTime.getStartDate()).toString().replace(".0", "Z").replace(" ", "T");
				if (!startDate.contains("Z")) startDate = startDate + "Z";
			}
			if(periodOfTime.getEndDate()!=null){
				endDate = Timestamp.valueOf(periodOfTime.getEndDate()).toString().replace(".0", "Z").replace(" ", "T");
				if (!endDate.contains("Z")) endDate = endDate + "Z";
			}
			tc.setStartDate(startDate);
			tc.setEndDate(endDate);
		}

		if (dp.getPublisher() != null) {
			List<DataServiceProvider> dataProviders = new ArrayList<>();
			for(LinkedEntity publisherLe : dp.getPublisher()) {
				Organization publisher = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(publisherLe);
				dataProviders.addAll(DataServiceProviderGeneration.getProviders(List.of(publisher)));
			}
			dataproduct.setDataProvider(dataProviders);
		}

		if(!dp.getContactPoint().isEmpty()) {
			for(LinkedEntity contactsLe : dp.getContactPoint()) {
				HashMap<String,Object> contact = new HashMap<String, Object>();
				ContactPoint contactpoint = (ContactPoint) LinkedEntityAPI.retrieveFromLinkedEntity(contactsLe);
				contact.put("id", contactpoint.getInstanceId());
				contact.put("metaid", contactpoint.getMetaId());
				contact.put("uid", contactpoint.getUid());
				if(contactpoint.getPerson()!=null) {
					Person person = (Person) LinkedEntityAPI.retrieveFromLinkedEntity(contactpoint.getPerson());
					HashMap<String,Object> relatedPerson = new HashMap<String, Object>();
					relatedPerson.put("id",person.getInstanceId());
					relatedPerson.put("metaid",person.getMetaId());
					relatedPerson.put("uid",person.getUid());
					contact.put("person", relatedPerson);
				}
				dataproduct.getContactPoints().add(contact);
			}
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + dp.getInstanceId()+"&contactType="+ProviderType.DATAPROVIDERS)
					.type(ProviderType.DATAPROVIDERS).build());
		}


		if (dp.getCategory() != null && !dp.getCategory().isEmpty()) {

			dataproduct.setScienceDomain(dp.getCategory().stream()
					.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.map(Category::getName)
					.collect(Collectors.toList()));
		}

		dataproduct.setAccessRights(dp.getAccessRight());

		if(dp.getProvenance()!=null && !dp.getProvenance().isEmpty()) {
			dp.getProvenance().forEach(instance -> {
				HashMap<String, String> prov = new HashMap<String, String>();
				prov.put("provenance", instance);
				dataproduct.getProvenance().add(prov);
			});
		}


		distribution.getRelatedDataProducts().add(dataproduct);

		/**
		 * 
		 * WebService bean population
		 * 
		 */
		if (ws != null) {
			Webservice webservice = new Webservice();

			for(LinkedEntity contacts : ws.getContactPoint()) {
				HashMap<String,Object> contact = new HashMap<String, Object>();
				ContactPoint contactpoint = (ContactPoint) LinkedEntityAPI.retrieveFromLinkedEntity(contacts);
				contact.put("id", contactpoint.getInstanceId());
				contact.put("metaid", contactpoint.getMetaId());
				contact.put("uid", contactpoint.getUid());
				if(contactpoint.getPerson()!=null) {
					Person person = (Person) LinkedEntityAPI.retrieveFromLinkedEntity(contactpoint.getPerson());
					HashMap<String,String> relatedPerson = new HashMap<String, String>();
					relatedPerson.put("id",person.getInstanceId());
					relatedPerson.put("metaid",person.getMetaId());
					relatedPerson.put("uid",person.getUid());
					contact.put("person", relatedPerson);
				}
				webservice.getContactPoints().add(contact);
			}

			webservice.setDescription(ws.getDescription());

			if (ws.getDocumentation() != null) {
				webservice.setDocumentation(ws.getDocumentation().stream()
						.map(linkedEntity -> (Documentation) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
						.map(Documentation::getUri)
						.collect(Collectors.joining(".")));
			}

			webservice.setName(Optional.ofNullable(ws.getName()).orElse(null));

			if (ws.getProvider() != null) {
				Organization provider = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(ws.getProvider());
				List<DataServiceProvider> serviceProviders = DataServiceProviderGeneration.getProviders(List.of(provider));
				if (!serviceProviders.isEmpty()){
					webservice.setProvider(serviceProviders.get(0));
				}
			}

			if (ws.getSpatialExtent() != null && !ws.getSpatialExtent().isEmpty()) {
				for (LinkedEntity sLe : ws.getSpatialExtent()) {
					Location s = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(sLe);
					webservice.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
				}
			}

			if (ws.getTemporalExtent() != null && !ws.getTemporalExtent().isEmpty()) {
				for(LinkedEntity temporalLe : ws.getTemporalExtent()) {
					PeriodOfTime temporal = (PeriodOfTime) LinkedEntityAPI.retrieveFromLinkedEntity(temporalLe);
					TemporalCoverage tcws = new TemporalCoverage();

					String startDate = null;
					String endDate = null;

					if(temporal.getStartDate()!=null){
						startDate = Timestamp.valueOf(temporal.getStartDate()).toString().replace(".0", "Z").replace(" ", "T");
						if (!startDate.contains("Z")) startDate = startDate + "Z";
					}
					if(temporal.getEndDate()!=null){
						endDate = Timestamp.valueOf(temporal.getEndDate()).toString().replace(".0", "Z").replace(" ", "T");
						if (!endDate.contains("Z")) endDate = endDate + "Z";
					}

                    tcws.setStartDate(startDate);
					tcws.setEndDate(endDate);
					webservice.getTemporalCoverage().add(tcws);
				}
			}
			if (ws.getCategory() != null) {
				webservice.setType(ws.getCategory().stream()
						.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
						.map(Category::getName)
						.collect(Collectors.toList()));
			}

			/**
			 * 
			 * WebService Operations
			 * 
			 */
			List<org.epos.eposdatamodel.Operation> operations = ws.getSupportedOperation().stream()
					.map(linkedEntity -> (org.epos.eposdatamodel.Operation) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.collect(Collectors.toList());

			// OPERATION AND PARAMETERS
			if (!operations.isEmpty()) {
				for(org.epos.eposdatamodel.Operation op : operations) {
					if(operationsIdRelatedToDistribution!=null && operationsIdRelatedToDistribution.contains(op.getInstanceId())){
						Operation operation = new Operation();

						operation.setMethod(op.getMethod());
						operation.setEndpoint(op.getTemplate());
						operation.setUid(op.getUid());

						if (op.getMapping() != null && !op.getMapping().isEmpty()) {
							for (LinkedEntity mpLe : op.getMapping()) {
								Mapping mp = (Mapping) LinkedEntityAPI.retrieveFromLinkedEntity(mpLe);
								ServiceParameter sp = new ServiceParameter();
								sp.setDefaultValue(mp.getDefaultValue());
								sp.setEnumValue(
										mp.getParamValue() != null ?
												mp.getParamValue()
												: new ArrayList<>()
										);
								sp.setName(mp.getVariable());
								sp.setMaxValue(mp.getMaxValue());
								sp.setMinValue(mp.getMinValue());
								sp.setLabel(mp.getLabel() != null ? mp.getLabel().replaceAll("@en", "") : null);
								sp.setProperty(mp.getProperty());
								sp.setRequired(Boolean.parseBoolean(mp.getRequired()));
								sp.setType(mp.getRange() != null ? mp.getRange().replace("xsd:", "") : null);
								sp.setValue(null);
								sp.setValuePattern(mp.getValuePattern());
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

		if(ws!=null && !ws.getContactPoint().isEmpty()) {
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + ws.getInstanceId()+"&contactType="+ProviderType.SERVICEPROVIDERS)
					.type(ProviderType.SERVICEPROVIDERS).build());
		}


		if(!dp.getContactPoint().isEmpty()) {
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + dp.getInstanceId()+"&contactType="+ProviderType.DATAPROVIDERS)
					.type(ProviderType.DATAPROVIDERS).build());
		}

		if((ws!=null && !ws.getContactPoint().isEmpty() && !dp.getContactPoint().isEmpty())){
			distribution.getAvailableContactPoints()
			.add(new AvailableContactPointsBuilder()
					.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + distribution.getId()+"&contactType="+ProviderType.ALL)
					.type(ProviderType.ALL).build());
		}


		distribution.setAvailableFormats(AvailableFormatsGeneration.generate(distributionSelected));

		// TEMP SECTION
		ArrayList<DiscoveryItem> discoveryList = new ArrayList<>();

		Set<String> facetsDataProviders = new HashSet<String>();
		if(dp.getPublisher() != null ) {
			for (LinkedEntity edmMetaIdLe : dp.getPublisher()) {
				Organization edmMetaId = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(edmMetaIdLe);
				if(edmMetaId.getLegalName() != null && !edmMetaId.getLegalName().isEmpty()) {
					facetsDataProviders.add(String.join(",",edmMetaId.getLegalName()));
				}
			}
		}

		Set<String> facetsServiceProviders = new HashSet<String>();
		if(ws!=null && ws.getProvider()!=null) {
			Organization edmMetaId = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(ws.getProvider());
			if(edmMetaId.getLegalName() != null && !edmMetaId.getLegalName().isEmpty()) {
				facetsServiceProviders.add(String.join(",",edmMetaId.getLegalName()));
			}
		}

		List<String> categoryList = dp.getCategory().stream()
				.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
				.map(Category::getUid)
				.filter(uid -> uid.contains("category:"))
				.collect(Collectors.toList());

		discoveryList.add(new DiscoveryItem.DiscoveryItemBuilder(distributionSelected.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId()+"?extended=true")
				.uid(distribution.getUid())
				.metaId(distribution.getMetaId())
				.title(distribution.getTitle()!=null?String.join(";",distribution.getTitle()):null)
				.description(distribution.getDescription()!=null? String.join(";",distribution.getDescription()):null)
				.availableFormats(AvailableFormatsGeneration.generate(distributionSelected))
				.sha256id(DigestUtils.sha256Hex(distribution.getUid()))
				.dataProvider(facetsDataProviders)
				.versioningStatus(distribution.getVersioningStatus())
				.serviceProvider(facetsServiceProviders)
				.categories(categoryList.isEmpty()? null : categoryList)
				.editorId(distribution.getEditorId())
				.build());

		FacetsNodeTree categories = FacetsGeneration.generateResponseUsingCategories(discoveryList, Facets.Type.DATA);
		categories.getNodes().forEach(node -> node.setDistributions(null));
		distribution.setCategories(categories.getFacets());

		return distribution;
	}

}
