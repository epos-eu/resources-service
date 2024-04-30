package org.epos.api.core.filtersearch;

import java.util.*;
import java.util.stream.Collectors;

import org.epos.api.beans.DataServiceProvider;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.utility.BBoxToPolygon;
import org.epos.handler.dbapi.model.EDMCategory;
import org.epos.handler.dbapi.model.EDMEdmEntityId;
import org.epos.handler.dbapi.model.EDMEquipment;
import org.epos.handler.dbapi.model.EDMEquipmentFacility;
import org.epos.handler.dbapi.model.EDMFacility;
import org.epos.handler.dbapi.model.EDMFacilityService;
import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationOwner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FacilityFilterSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityFilterSearch.class); 


	private static final String NORTHEN_LAT  = "epos:northernmostLatitude";
	private static final String SOUTHERN_LAT  = "epos:southernmostLatitude";
	private static final String WESTERN_LON  = "epos:westernmostLongitude";
	private static final String EASTERN_LON  = "epos:easternmostLongitude";

	private static final String PARAMETER_FACILITY_TYPES = "facilitytypes";
	private static final String PARAMETER_EQUIPMENT_TYPES = "equipmenttypes";

	public static List<EDMFacility> doFilters(List<EDMFacility> facilityList, Map<String,Object> parameters, List<EDMCategory> categories, List<EDMOrganization> organizationForOwners) {

		facilityList = filterFacilityByFullText(facilityList, parameters);
		facilityList = filterFacilityByKeywords(facilityList, parameters);
		facilityList = filterFacilityByOrganizations(facilityList, parameters, organizationForOwners);
		facilityList = filterFacilityByBoundingBox(facilityList, parameters);
		facilityList = filterByFacilityType(facilityList, parameters, categories);
		facilityList = filterByEquipmentType(facilityList, parameters, categories);

		return facilityList;
	}

	private static List<EDMFacility> filterByFacilityType(List<EDMFacility> facilityList, Map<String,Object> parameters, List<EDMCategory> categories) {

		if(parameters.containsKey(PARAMETER_FACILITY_TYPES)) {
			ArrayList<EDMFacility> tempFacilityList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER_FACILITY_TYPES).toString().split(","));
			facilityList.forEach(facility -> {
				List<String> facilityTypes = new ArrayList<String>();
				categories
				.stream()
				.filter(cat -> cat.getUid().equals(facility.getType()))
				.map(EDMCategory::getId)
				.forEach(facilityTypes::add);

				if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
					tempFacilityList.add(facility);
				}
			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<EDMFacility> filterByEquipmentType(List<EDMFacility> facilityList, Map<String,Object> parameters, List<EDMCategory> categories) {

		if(parameters.containsKey(PARAMETER_EQUIPMENT_TYPES)) {
			ArrayList<EDMFacility> tempFacilityList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER_EQUIPMENT_TYPES).toString().split(","));
			facilityList.forEach(facility -> {
				List<String> facilityTypes = new ArrayList<String>();
				if(facility.getEquipmentFacilitiesByInstanceId()!=null) {
					for(EDMEquipmentFacility item : facility.getEquipmentFacilitiesByInstanceId()) {
						categories
						.stream()
						.filter(cat -> cat.getUid().equals(item.getEquipmentByInstanceEquipmentId().getType()))
						.map(EDMCategory::getId)
						.forEach(facilityTypes::add);
					}
					if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
						tempFacilityList.add(facility);
					}
				}
			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<EDMFacility> filterFacilityByBoundingBox(List<EDMFacility> facilityList, Map<String,Object> parameters) {
		//check if the bbox passed inside the parameters is complete, if not exit and return the whole list of facility
		if (!parameters.containsKey(NORTHEN_LAT)
				|| !parameters.containsKey(SOUTHERN_LAT)
				|| !parameters.containsKey(WESTERN_LON)
				|| !parameters.containsKey(EASTERN_LON))
			return facilityList;
		GeometryFactory geometryFactory = new GeometryFactory();
		WKTReader reader = new WKTReader( geometryFactory );
		try {
			//try to parse the bbox
			final Geometry inputGeometry = reader.read(BBoxToPolygon.transform(parameters));
			//check if the bbox is parsed
			if(inputGeometry!=null) {
				//temporary facility list, it will collect the facility which are, even in part, contained inside the bbox
				ArrayList<EDMFacility> tempFacilityList = new ArrayList<>();
				//utility set to contain the all the uid of the selected facility to avoid duplicates
				Set<String> uidSet = new HashSet<>();
				//iterate over every dataproduct
				for (EDMFacility fac : facilityList) {
					//if the uid belong to an already selected facility just skip the iteration
					if(uidSet.contains(fac.getMetaId())) continue;
					//iterate over every distribution related to the facility taken into account
					fac.getFacilitySpatialsByInstanceId()
					.forEach(spatial -> {
						if(Objects.nonNull(spatial.getLocation())){
							try {
								//parse the spatial of the webservice
								Geometry dsGeometry = reader.read(spatial.getLocation());
								//if the facility hasn't been selected yet and the spatial of the facility
								//intersect with the bbox, the facility is selected.
								if (!uidSet.contains(fac.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
									tempFacilityList.add(fac);
									uidSet.add(fac.getMetaId());
								}
							} catch (ParseException e) {
								LOGGER.error("Error occurs during BBOX dataproduct parsing", e);
							}
						}
					});
				}
				//replace the old facility list with the new temporary filtered facility list
				facilityList = tempFacilityList;
			}
		} catch (org.locationtech.jts.io.ParseException e) {
			LOGGER.error("Error occurs during BBOX input parsing ",e);
		}
		return facilityList;
	}


	private static List<EDMFacility> filterFacilityByOrganizations(List<EDMFacility> facilityList, Map<String,Object> parameters, List<EDMOrganization> organizationForOwners) {
		if(parameters.containsKey("organisations")) {
			List<String> organisations = Arrays.asList(parameters.get("organisations").toString().split(","));

			HashSet<EDMFacility> tempFacilityList = new HashSet<>();
			facilityList.forEach(fac -> {

				List<EDMEdmEntityId> organizationsEntityIds = new ArrayList<>();

				organizationForOwners.stream()
				.map(EDMOrganization::getOwnsByInstanceId)
				.filter(Objects::nonNull)
				.forEach(organizationowner->{
					organizationowner.stream()
					.filter(edmEntity -> edmEntity.getEntityMetaId().equals(fac.getMetaId()))
					.map(EDMOrganizationOwner::getOrganizationByInstanceOrganizationId)
					.map(EDMOrganization::getEdmEntityIdByMetaId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList())
					.forEach(organizationsEntityIds::add);

				});


				List<DataServiceProvider> providers = new ArrayList<DataServiceProvider>();
				providers.addAll(DataServiceProviderGeneration.getProviders(organizationsEntityIds));
				List<String> value = new ArrayList<>();

				providers.forEach(resource->{
					value.add(resource.getInstanceid());
					//LOGGER.info("Number of related retrieved "+resource.getRelatedDataProvider().size());
					resource.getRelatedDataProvider().forEach(related ->{
						value.add(related.getInstanceid());
					});
					//LOGGER.info("Number of related retrieved "+resource.getRelatedDataServiceProvider().size());
					resource.getRelatedDataServiceProvider().forEach(related ->{
						value.add(related.getInstanceid());
					});
				});

				if(!Collections.disjoint(organisations, value)){
					tempFacilityList.add(fac);
				}

			});
			facilityList = new ArrayList<>(tempFacilityList);
		}
		return facilityList;
	}


	private static List<EDMFacility> filterFacilityByKeywords(List<EDMFacility> facilityList, Map<String,Object> parameters) {
		if(parameters.containsKey("keywords")) {
			ArrayList<EDMFacility> tempFacilityList = new ArrayList<>();
			String[] keywords = parameters.get("keywords").toString().split(",");

			facilityList.forEach(fac -> {
				Collection<EDMFacilityService> edmFacilityService = fac.getFacilityServicesByInstanceId() != null ? fac.getFacilityServicesByInstanceId() : null;
				if (edmFacilityService!=null) {
					edmFacilityService.forEach(instanceWS ->{
						if(Objects.nonNull(instanceWS.getServiceByInstanceServiceId().getKeywords())){
							List<String> dataproductKeywords = Arrays.stream(instanceWS.getServiceByInstanceServiceId().getKeywords().split(","))
									.map(String::toLowerCase)
									.map(String::trim)
									.collect(Collectors.toList());

							if(!Collections.disjoint(dataproductKeywords.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(keywords))){
								tempFacilityList.add(fac);
							}
						}
					});
				}

			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<EDMFacility> filterFacilityByFullText(List<EDMFacility> facilityList, Map<String,Object> parameters) {
		if(parameters.containsKey("q")) {
			HashSet<EDMFacility> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").toString().toLowerCase().split(",");

			for (EDMFacility edmFacility : facilityList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));

				Collection<EDMFacilityService> edmFacilityService = edmFacility.getFacilityServicesByInstanceId() != null ? edmFacility.getFacilityServicesByInstanceId() : null;
				if (edmFacilityService!=null) {
					edmFacilityService.forEach(instanceWS ->{
						if(Objects.nonNull(instanceWS.getServiceByInstanceServiceId().getKeywords())){
							List<String> dataproductKeywords = Arrays.stream(instanceWS.getServiceByInstanceServiceId().getKeywords().split(","))
									.map(String::toLowerCase)
									.map(String::trim)
									.collect(Collectors.toList());

							for (String q : qSMap.keySet()) {
								if (dataproductKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
							}
						}
						
						if(Objects.nonNull(instanceWS.getServiceByInstanceServiceId().getUid())){
							for (String q : qSMap.keySet()) {
								if (instanceWS.getServiceByInstanceServiceId().getUid().contains(q)) qSMap.put(q, Boolean.TRUE);
							}
						}
						
					});
				}

				if (edmFacility.getTitle() != null) {
					for (String q : qSMap.keySet()) {
						if (edmFacility.getTitle().toLowerCase().contains(q)) {
							qSMap.put(q, Boolean.TRUE);
						}
					}
				}


				if (edmFacility.getDescription() != null) {
					for (String q : qSMap.keySet()) {
						if (edmFacility.getDescription().toLowerCase().contains(q)) {
							qSMap.put(q, Boolean.TRUE);
						}
					}
				}
				
				if(Objects.nonNull(edmFacility.getUid())){
					for (String q : qSMap.keySet()) {
						if (edmFacility.getUid().contains(q)) qSMap.put(q, Boolean.TRUE);
					}
				}

				if (qSMap.values().stream().allMatch(b -> b)) tempDatasetList.add(edmFacility);
			}

			facilityList = new ArrayList<>(tempDatasetList);
		}

		return facilityList;

	}

}
