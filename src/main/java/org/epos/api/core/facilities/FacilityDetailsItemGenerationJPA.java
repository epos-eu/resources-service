package org.epos.api.core.facilities;

import org.epos.api.beans.AvailableContactPoints.AvailableContactPointsBuilder;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.Facility;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.beans.TemporalCoverage;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.enums.ProviderType;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
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

public class FacilityDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityDetailsItemGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/facilities/details/";
	private static final String EMAIL_SENDER = EnvironmentVariables.API_CONTEXT + "/sender/send-email?id=";
	private static final String API_PATH_EXECUTE  = EnvironmentVariables.API_CONTEXT+"/equipmentsexecute/";
	private static final String API_FORMAT = "?format=";

	public static Facility generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		EntityManager em = new DBService().getEntityManager();

		List<EDMFacility> facilitySelectedList = getFromDB(em, EDMFacility.class,
				"facility.findAllByMetaId",
				"METAID", parameters.get("id"));

		if (facilitySelectedList.stream().noneMatch(facSelected -> facSelected.getState().equals("PUBLISHED")))
			return null;

		EDMFacility facilitySelected = facilitySelectedList.stream().filter(facSelected -> facSelected.getState().equals("PUBLISHED")).collect(Collectors.toList()).get(0);

		if (facilitySelected == null) return null;


		Collection<EDMFacilityService> ws = facilitySelected.getFacilityServicesByInstanceId() != null ? facilitySelected.getFacilityServicesByInstanceId() : null;
		if (ws == null && facilitySelected.getFacilitiesByInstanceId() != null) return null;


		Facility facility = new Facility();

		if (facilitySelected.getTitle() != null) facility.setTitle(facilitySelected.getTitle());
		if (facilitySelected.getDescription() != null) facility.setDescription(facilitySelected.getDescription());

		if (facilitySelected.getType() != null) {
			String[] type = facilitySelected.getType().split("\\/");
			facility.setType(type[type.length - 1]);
		}

		facility.setId(Optional.ofNullable(facilitySelected.getMetaId()).orElse(null));
		facility.setUid(Optional.ofNullable(facilitySelected.getUid()).orElse(null));



		facility.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId());

		List<String> keywords = new ArrayList<String>();
		if(ws!=null)
			ws.forEach(instanceWS ->{
				instanceWS.getServiceByInstanceServiceId().getKeywords();
				keywords.addAll(Arrays.stream(Optional.ofNullable(instanceWS.getServiceByInstanceServiceId().getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
			});

		keywords.removeAll(Collections.singleton(null));
		keywords.removeAll(Collections.singleton(""));
		facility.setKeywords(new ArrayList<>(keywords));

		if (facilitySelected.getFacilitySpatialsByInstanceId() != null) {
			for (EDMFacilitySpatial s : facilitySelected.getFacilitySpatialsByInstanceId())
				facility.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
		}


		if (facilitySelected.getEdmEntityIdByOwner() != null) {
			List<DataServiceProvider> dataProviders = DataServiceProviderGeneration.getProviders(List.of(facilitySelected.getEdmEntityIdByOwner()));
			facility.setFacilityProvider(dataProviders);
		}



		// WEBSERVICE INFO
		if (ws != null) {

			ws.forEach(singleInstanceWS ->{
				EDMService singleWS = singleInstanceWS.getServiceByInstanceServiceId();
				facility.setServiceDescription(Optional.ofNullable(singleWS.getDescription()).orElse(null));
				facility.setServiceName(Optional.ofNullable(singleWS.getName()).orElse(null));



				if (singleWS.getServiceSpatialsByInstanceId() != null) {
					for (EDMServiceSpatial s : singleWS.getServiceSpatialsByInstanceId())
						facility.getServiceSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
				}

				TemporalCoverage tcws = new TemporalCoverage();
				if (singleWS.getServiceTemporalsByInstanceId() != null && singleWS.getServiceTemporalsByInstanceId().size() > 0) {
					Timestamp startdate = new ArrayList<>(singleWS.getServiceTemporalsByInstanceId()).get(0).getStartdate();
					Timestamp enddate = new ArrayList<>(singleWS.getServiceTemporalsByInstanceId()).get(0).getEnddate();

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
				}
				facility.setServiceTemporalCoverage(tcws);

				if (singleWS.getServiceCategoriesByInstanceId() != null) {
					facility.setServiceType(Optional.of(singleWS.getServiceCategoriesByInstanceId().stream()
							.map(EDMServiceCategory::getCategoryByCategoryId)
							.map(EDMCategory::getName).collect(Collectors.toList())).orElse(null));
				}
				if (!singleWS.getContactpointServicesByInstanceId().isEmpty()) {
					facility.getAvailableContactPoints()
					.add(new AvailableContactPointsBuilder()
							.href(EnvironmentVariables.API_HOST + EMAIL_SENDER + singleWS.getInstanceId()+"&contactType="+ProviderType.SERVICEPROVIDERS)
							.type(ProviderType.SERVICEPROVIDERS).build());
				}
			});

		}

		facility.setAvailableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
				.originalFormat("application/epos.geo+json")
				.format("application/epos.geo+json")
				.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + facilitySelected.getMetaId() + API_FORMAT + "application/epos.geo+json")
				.label("GEOJSON")
				.description(AvailableFormatType.CONVERTED)
				.build()));

		// TEMP SECTION
		ArrayList<DiscoveryItem> discoveryList = new ArrayList<>();

		Set<String> facetsDataProviders = new HashSet<String>();
		if(facilitySelected.getEdmEntityIdByOwner() != null ) {
			if (facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId() != null && !facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId().isEmpty()) {
				ArrayList<EDMOrganization> list = new ArrayList<>(facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId());
				list.sort(EDMUtil::compareEntityVersion);
				EDMOrganization edmDataproductRelatedOrganization = list.get(0);
				if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
					facetsDataProviders.add(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
				}
			}

		}

		List<String> categoryList = facilitySelected.getFacilityCategoriesByInstanceId().stream()
				.map(EDMFacilityCategory::getCategoryByCategoryId)
				.filter(Objects::nonNull)
				.filter(e->e.getUid().contains("category:"))
				.map(EDMCategory::getUid)
				.collect(Collectors.toList());
		
		
		System.out.println(categoryList);

		discoveryList.add(new DiscoveryItemBuilder(facilitySelected.getMetaId(),
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId())
				.uid(facility.getUid())
				.title(facility.getTitle())
				.description(facility.getDescription())
				.availableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
						.originalFormat("application/epos.geo+json")
						.format("application/epos.geo+json")
						.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + facilitySelected.getMetaId() + API_FORMAT + "application/epos.geo+json")
						.label("GEOJSON")
						.description(AvailableFormatType.CONVERTED)
						.build()))
				.setFacilityProvider(facetsDataProviders)
				.setCategories(categoryList.isEmpty() ? null : categoryList)
				.build());

		FacetsNodeTree categories = FacetsGeneration.generateResponseUsingCategories(discoveryList);
		categories.getNodes().forEach(node -> node.setDistributions(null));
		facility.setCategories(categories.getFacets());

		em.close();

		return facility;
	}

}