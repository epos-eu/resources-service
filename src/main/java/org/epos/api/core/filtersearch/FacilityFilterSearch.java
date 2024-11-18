package org.epos.api.core.filtersearch;

import java.util.*;
import java.util.stream.Collectors;

import commonapis.LinkedEntityAPI;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.utility.BBoxToPolygon;
import org.epos.eposdatamodel.*;
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

	public static List<Facility> doFilters(List<Facility> facilityList, Map<String,Object> parameters, List<Category> categories, List<Organization> organizationForOwners) {

		facilityList = filterFacilityByFullText(facilityList, parameters);
		facilityList = filterFacilityByKeywords(facilityList, parameters);
		facilityList = filterFacilityByOrganizations(facilityList, parameters, organizationForOwners);
		facilityList = filterFacilityByBoundingBox(facilityList, parameters);
		facilityList = filterByFacilityType(facilityList, parameters, categories);
		facilityList = filterByEquipmentType(facilityList, parameters, categories);

		return facilityList;
	}

	private static List<Facility> filterByFacilityType(List<Facility> facilityList, Map<String,Object> parameters, List<Category> categories) {

		if(parameters.containsKey(PARAMETER_FACILITY_TYPES)) {
			ArrayList<Facility> tempFacilityList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER_FACILITY_TYPES).toString().split(","));
			facilityList.forEach(facility -> {
				List<String> facilityTypes = new ArrayList<String>();
				categories
				.stream()
				.filter(cat -> cat.getUid().equals(facility.getType()))
				.map(Category::getInstanceId)
				.forEach(facilityTypes::add);

				if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
					tempFacilityList.add(facility);
				}
			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<Facility> filterByEquipmentType(List<Facility> facilityList, Map<String,Object> parameters, List<Category> categories) {

		if(parameters.containsKey(PARAMETER_EQUIPMENT_TYPES)) {
			ArrayList<Facility> tempFacilityList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER_EQUIPMENT_TYPES).toString().split(","));
			facilityList.forEach(facility -> {
				List<String> facilityTypes = new ArrayList<String>();
				if(facility.getIsPartOf()!=null) {
//	TODO:				for(LinkedEntity item : facility.getIsPartOf()) {
//
//						categories
//						.stream()
//						.filter(cat -> cat.getUid().equals(item.getEquipmentByInstanceEquipmentId().getType()))
//						.map(Category::getId)
//						.forEach(facilityTypes::add);
//					}
//					if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
//						tempFacilityList.add(facility);
//					}
				}
			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<Facility> filterFacilityByBoundingBox(List<Facility> facilityList, Map<String,Object> parameters) {
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
				ArrayList<Facility> tempFacilityList = new ArrayList<>();
				//utility set to contain the all the uid of the selected facility to avoid duplicates
				Set<String> uidSet = new HashSet<>();
				//iterate over every dataproduct
				for (Facility fac : facilityList) {
					//if the uid belong to an already selected facility just skip the iteration
					if(uidSet.contains(fac.getMetaId())) continue;
					//iterate over every distribution related to the facility taken into account
					if(fac.getSpatialExtent() != null){
						for (LinkedEntity spatialLe : fac.getSpatialExtent()) {
							Location wsSpatial = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(spatialLe);
							try {
								//parse the spatial of the webservice
								Geometry dsGeometry = reader.read(wsSpatial.getLocation());
								//if the dataproduct hasn't been selected yet and the spatial of the webservice
								//intersect with the bbox, the dataproduct is selected.
								if (!uidSet.contains(fac.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
									tempFacilityList.add(fac);
									uidSet.add(fac.getMetaId());
								}
							} catch (ParseException e) {
								LOGGER.error("Error occurs during BBOX dataproduct parsing", e);
							}
						}
					}
				}
				//replace the old facility list with the new temporary filtered facility list
				facilityList = tempFacilityList;
			}
		} catch (org.locationtech.jts.io.ParseException e) {
			LOGGER.error("Error occurs during BBOX input parsing ",e);
		}
		return facilityList;
	}


	private static List<Facility> filterFacilityByOrganizations(List<Facility> facilityList, Map<String,Object> parameters, List<Organization> organizationForOwners) {
		if(parameters.containsKey("organisations")) {
			List<String> organisations = Arrays.asList(parameters.get("organisations").toString().split(","));

			HashSet<Facility> tempFacilityList = new HashSet<>();
			facilityList.forEach(fac -> {

				List<Organization> organizationsEntityIds = new ArrayList<>();
//TODO:
//				organizationForOwners.stream()
//				.map(Organization::getOwns)
//				.filter(Objects::nonNull)
//				.forEach(organizationowner->{
//					organizationowner.stream()
//					.filter(edmEntity -> edmEntity.getMetaId().equals(fac.getMetaId()))
//					.map(OrganizationOwner::getOrganizationByInstanceOrganizationId)
//					.map(Organization::getEdmEntityIdByMetaId)
//					.filter(Objects::nonNull)
//					.collect(Collectors.toList())
//					.forEach(organizationsEntityIds::add);
//
//				});


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


	private static List<Facility> filterFacilityByKeywords(List<Facility> facilityList, Map<String,Object> parameters) {
		if(parameters.containsKey("keywords")) {
			ArrayList<Facility> tempFacilityList = new ArrayList<>();
			String[] keywords = parameters.get("keywords").toString().split(",");
			facilityList.forEach(fac -> {
				if(Objects.nonNull(fac.getKeywords())){
					List<String> dataproductKeywords = Arrays.stream(fac.getKeywords().split(","))
							.map(String::toLowerCase)
							.map(String::trim)
							.collect(Collectors.toList());

					if(!Collections.disjoint(dataproductKeywords.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(keywords))){
						tempFacilityList.add(fac);
					}
				}
			});
			facilityList = tempFacilityList;
		}
		return facilityList;
	}

	private static List<Facility> filterFacilityByFullText(List<Facility> facilityList, Map<String,Object> parameters) {
		if(parameters.containsKey("q")) {
			HashSet<Facility> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").toString().toLowerCase().split(",");

			for (Facility edmFacility : facilityList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));

				if(Objects.nonNull(edmFacility.getKeywords())){
					List<String> dataproductKeywords = Arrays.stream(edmFacility.getKeywords().split(","))
							.map(String::toLowerCase)
							.map(String::trim)
							.collect(Collectors.toList());

					for (String q : qSMap.keySet()) {
						if (dataproductKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
					}
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
