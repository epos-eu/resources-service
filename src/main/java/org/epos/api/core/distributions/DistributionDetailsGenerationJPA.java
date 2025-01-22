package org.epos.api.core.distributions;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableContactPoints.AvailableContactPointsBuilder;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.Distribution;
import org.epos.api.beans.ServiceParameter;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.beans.TemporalCoverage;
import org.epos.api.enums.ProviderType;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class DistributionDetailsGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionDetailsGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/resources/details/";
	private static final String EMAIL_SENDER = EnvironmentVariables.API_CONTEXT + "/sender/send-email?id=";

	public static Distribution generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		org.epos.eposdatamodel.Distribution distributionSelected = (org.epos.eposdatamodel.Distribution) AbstractAPI.retrieveAPI(EntityNames.DISTRIBUTION.name()).retrieve(parameters.get("id").toString());


		if (distributionSelected == null) return null;

		DataProduct dp;
		if (distributionSelected.getDataProduct() != null &&
				!distributionSelected.getDataProduct().isEmpty()) {
			dp = (DataProduct) LinkedEntityAPI.retrieveFromLinkedEntity(distributionSelected.getDataProduct().get(0));
			if (dp == null) return null;
		} else {
			return null;
		}

		WebService ws = distributionSelected.getAccessService() != null && !distributionSelected.getAccessService().isEmpty() ?
				(WebService) LinkedEntityAPI.retrieveFromLinkedEntity(distributionSelected.getAccessService().get(0)) : null;
		if (ws == null && distributionSelected.getAccessService() != null) return null;

		Operation op = null;
		if (distributionSelected.getSupportedOperation() != null) {
			List<Operation> opList = distributionSelected.getSupportedOperation().parallelStream()
			.map(linkedEntity -> (Operation) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.filter(Objects::nonNull).collect(Collectors.toList());
			op = !opList.isEmpty() ? opList.get(0) : null;
		} else {
			return null;
		}

		Distribution distribution = new Distribution();

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

		if (distributionSelected.getDownloadURL() != null) {
			distribution.setDownloadURL(
					Optional.of(
							String.join(".", distributionSelected.getDownloadURL())
					).orElse(null)
			);
		}

		if (distributionSelected.getAccessURL() != null) {
			distribution.setEndpoint(
					Optional.of(
                            String.join(".", distributionSelected.getAccessURL())
							).orElse(null)
					);
		}

		distribution.setLicense(distributionSelected.getLicence());

		// DATASET INFO
		ArrayList<String> internalIDs = new ArrayList<>();
		List<String> doi = new ArrayList<>();
		if(dp.getIdentifier()!=null)
			dp.getIdentifier().forEach(identifierLe -> {
				Identifier identifier = (Identifier) LinkedEntityAPI.retrieveFromLinkedEntity(identifierLe);
				if (identifier.getType().equals("DOI")) {
					doi.add(identifier.getIdentifier());
					distribution.setDOI(doi);
				}
				if (identifier.getType().equals("DDSS-ID")) {
					String ddss = identifier.getIdentifier();
					if (ddss != null) internalIDs.add(ddss);
				}
			});


		distribution.setFrequencyUpdate(dp.getAccrualPeriodicity());
		distribution.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId());
		distribution.setHrefExtended(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId()+"?extended=true");
		distribution.setInternalID(internalIDs);

		Set<String> keywords = new HashSet<>(Arrays.stream(Optional.ofNullable(dp.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
		if (ws != null)
			keywords.addAll(Arrays.stream(Optional.ofNullable(ws.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
		keywords.removeAll(Collections.singleton(null));
		keywords.removeAll(Collections.singleton(""));
		distribution.setKeywords(new ArrayList<>(keywords));

		if (dp.getSpatialExtent() != null) {
			for (LinkedEntity sLe : dp.getSpatialExtent()) {
				Location s = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(sLe);
				distribution.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
			}
		}

		// how to handle multiple temporal? at the moment use only the first one
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
		distribution.setTemporalCoverage(tc);

		if (dp.getPublisher() != null) {
			List<DataServiceProvider> dataProviders = DataServiceProviderGeneration.getProviders(
					dp.getPublisher().parallelStream()
							.map(linkedEntity -> (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
							.filter(Objects::nonNull).collect(Collectors.toList())
			);
			distribution.setDataProvider(dataProviders);
		}

		if (dp.getCategory() != null) {
			distribution.setScienceDomain(
					dp.getCategory().parallelStream()
							.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
							.filter(Objects::nonNull)
							.map(Category::getName)
							.collect(Collectors.toList()));
		}

		distribution.setHasQualityAnnotation(dp.getHasQualityAnnotation());

		// WEBSERVICE INFO
		if (ws != null) {
			distribution.setServiceDescription(ws.getDescription());

			if (ws.getDocumentation() != null) {
				distribution.setServiceDocumentation(ws.getDocumentation().parallelStream()
						.map(linkedEntity -> (Documentation) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
						.map(Documentation::getUri)
						.collect(Collectors.joining(".")));
			}

			distribution.setServiceName(Optional.ofNullable(ws.getName()).orElse(null));

			if (ws.getProvider() != null) {
				List<DataServiceProvider> serviceProviders = DataServiceProviderGeneration.getProviders(List.of((Organization) LinkedEntityAPI.retrieveFromLinkedEntity(ws.getProvider())));
				if (!serviceProviders.isEmpty()){
					distribution.setServiceProvider(serviceProviders.get(0));
				}
			}

			if (ws.getSpatialExtent() != null&& !ws.getSpatialExtent().isEmpty()) {
				for (LinkedEntity sLe : ws.getSpatialExtent()) {
					Location s = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(sLe);
					distribution.getServiceSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
				}
			}

			TemporalCoverage tcws = new TemporalCoverage();
			if (ws.getTemporalExtent() != null && !ws.getTemporalExtent().isEmpty()) {
				PeriodOfTime periodOfTime = (PeriodOfTime) LinkedEntityAPI.retrieveFromLinkedEntity(ws.getTemporalExtent().get(0));
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
				tcws.setStartDate(startDate);
				tcws.setEndDate(endDate);
			}
			distribution.setServiceTemporalCoverage(tcws);

			if (ws.getCategory() != null) {
				distribution.setServiceType(
						ws.getCategory().parallelStream()
								.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
								.filter(Objects::nonNull)
								.map(Category::getName)
								.collect(Collectors.toList()));
			}

		}

		// CONTACT POINTS

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

		distribution.setParameters(new ArrayList<>());
		// OPERATION AND PARAMETERS
		if (Objects.nonNull(op)) {
			distribution.setEndpoint(op.getTemplate());
			if(op.getTemplate()!=null) distribution.setServiceEndpoint(op.getTemplate().split("\\{")[0]);
			distribution.setOperationid(op.getUid());
			if (op.getMapping() != null && !op.getMapping().isEmpty()) {
				for (LinkedEntity mpLe : op.getMapping()) {
					Mapping mp = (Mapping) LinkedEntityAPI.retrieveFromLinkedEntity(mpLe);
					ServiceParameter sp = getServiceParameter(mp);
					distribution.getParameters().add(sp);
				}
			}
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

		List<String> categoryList = dp.getCategory().parallelStream()
				.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
				.map(Category::getUid)
				.filter(uid -> uid.contains("category:"))
				.collect(Collectors.toList());

		discoveryList.add(new DiscoveryItem.DiscoveryItemBuilder(distributionSelected.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getInstanceId()+"?extended=true")
				.uid(distribution.getUid())
				.title(distribution.getTitle()!=null?String.join(";",distribution.getTitle()):null)
				.description(distribution.getDescription()!=null? String.join(";",distribution.getDescription()):null)
				.availableFormats(AvailableFormatsGeneration.generate(distributionSelected))
				.setSha256id(DigestUtils.sha256Hex(distribution.getUid()))
				.setDataprovider(facetsDataProviders)
				.setServiceProvider(facetsServiceProviders)
				.setCategories(categoryList.isEmpty()? null : categoryList)
				.build());
		
		FacetsNodeTree categories = FacetsGeneration.generateResponseUsingCategories(discoveryList);
		categories.getNodes().forEach(node -> node.setDistributions(null));
		distribution.setCategories(categories.getFacets());
		
		return distribution;
	}

	private static ServiceParameter getServiceParameter(Mapping mp) {
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
		sp.setRequired(mp.getRequired()!=null? Boolean.parseBoolean(mp.getRequired()):null);
		sp.setType(mp.getRange() != null ? mp.getRange().replace("xsd:", "") : null);
		sp.setValue(null);
		sp.setValuePattern(mp.getValuePattern());
		sp.setVersion(null);
		sp.setReadOnlyValue(Boolean.parseBoolean(mp.getReadOnlyValue())? mp.getReadOnlyValue() : null);
		sp.setMultipleValue(Boolean.parseBoolean(mp.getMultipleValues())? mp.getMultipleValues() : null);
		return sp;
	}

}