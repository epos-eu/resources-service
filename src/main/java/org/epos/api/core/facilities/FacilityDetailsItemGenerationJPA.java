package org.epos.api.core.facilities;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import model.StatusType;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.Facility;
import org.epos.api.beans.ServiceParameter;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Location;
import org.epos.eposdatamodel.Organization;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.epos.library.objects.LinkObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FacilityDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityDetailsItemGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/facilities/details/";
	private static final String API_PATH_EXECUTE_EQUIPMENTS  = EnvironmentVariables.API_CONTEXT+"/equipments/";
	private static final String API_FORMAT = "?format=";

	public static Object generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		org.epos.eposdatamodel.Facility facilitySelected = (org.epos.eposdatamodel.Facility) AbstractAPI.retrieveAPI(EntityNames.FACILITY.name()).retrieve(parameters.get("id").toString());
		List<Organization> organizationForOwners  = (List<Organization>) AbstractAPI.retrieveAPI(EntityNames.ORGANIZATION.name()).retrieveAll().stream().filter(item -> ((org.epos.eposdatamodel.Organization) item).getStatus().equals(StatusType.PUBLISHED)).collect(Collectors.toList());
		List<Category> categoriesFromDB = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll().stream().filter(item -> ((org.epos.eposdatamodel.Category) item).getStatus().equals(StatusType.PUBLISHED)).collect(Collectors.toList());

		if(parameters.containsKey("format") && parameters.get("format").toString().equals("application/epos.geo+json"))
			return generateAsGeoJson(facilitySelected, parameters.containsKey("equipmenttypes")? parameters.get("equipmenttypes").toString() : null);
		else {
			//TODO:
			//Collection<EDMFacilityService> ws = facilitySelected.getFacilityServicesByInstanceId() != null ? facilitySelected.getFacilityServicesByInstanceId() : null;
			//if (ws == null && facilitySelected.getFacilitiesByInstanceId() != null) return null;


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
			keywords.addAll(Arrays.stream(Optional.ofNullable(facilitySelected.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
			
			keywords.removeAll(Collections.singleton(null));
			keywords.removeAll(Collections.singleton(""));
			facility.setKeywords(new ArrayList<>(keywords));


			// Facility Types
			List<Category> type = categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(facilitySelected.getType())).collect(Collectors.toList());


			facility.setType(type.get(0).getName());


			if (facilitySelected.getSpatialExtent() != null) {
				for (LinkedEntity s : facilitySelected.getSpatialExtent()) {
					Location location = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(s);
					facility.getSpatial().addPaths(SpatialInformation.doSpatial(location.getLocation()), SpatialInformation.checkPoint(location.getLocation()));
				}
			}

			Set<Organization> organizationsEntityIds = new HashSet<>();

//TODO:			organizationForOwners.stream()
//			.map(Organization::getOwns)
//			.filter(Objects::nonNull)
//			.forEach(organizationowner->{organizationowner.stream()
//				.filter(edmEntity -> edmEntity.getEntityMetaId().equals(facilitySelected.getMetaId()))
//				.map(EDMOrganizationOwner::getOrganizationByInstanceOrganizationId)
//				.map(EDMOrganization::getEdmEntityIdByMetaId)
//				.filter(Objects::nonNull)
//				.collect(Collectors.toList())
//				.forEach(organizationsEntityIds::add);
//
//			});


			facility.setDataProvider(DataServiceProviderGeneration.getProviders(new ArrayList<Organization>(organizationsEntityIds)));
			facilitySelected.getPageURL().forEach(page->{
				facility.getPage().add(page);
			});

			Set<Category> equipmentTypes = new HashSet<>();

			//Equipment types
//TODO:			if(facilitySelected.getEquipmentFacilitiesByInstanceId()!=null) {
//				for(EDMEquipmentFacility item : facilitySelected.getEquipmentFacilitiesByInstanceId()) {
//					categoriesFromDB
//					.stream()
//					.filter(cat -> cat.getUid().equals(item.getEquipmentByInstanceEquipmentId().getType()))
//					.forEach(equipmentTypes::add);
//				}
//			}

			facility.setServiceParameters(new ArrayList<>());
			ServiceParameter sp = new ServiceParameter();
			sp.setName("equipmenttypes");
			sp.setLabel("Equipment types");
			sp.setEnumValue(
					equipmentTypes.size()>0 ?
							equipmentTypes.stream().map(Category::getName).collect(Collectors.toList())
							: new ArrayList<>()
					);
			sp.setRequired(false);
			sp.setType("string");
			sp.setMultipleValue("true");
			facility.getServiceParameters().add(sp);

			facility.setAvailableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat("application/epos.geo+json")
					.format("application/epos.geo+json")
					.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_EQUIPMENTS + "all"+ API_FORMAT + "application/epos.geo+json"+"&facilityid=" + facilitySelected.getMetaId())
					.label("GEOJSON")
					.description(AvailableFormatType.CONVERTED)
					.build()));

			// TEMP SECTION
			ArrayList<DiscoveryItem> discoveryList = new ArrayList<>();

//TODO:			Set<String> facetsDataProviders = new HashSet<String>();
//			if(facilitySelected.getEdmEntityIdByOwner() != null ) {
//				if (facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId() != null && !facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId().isEmpty()) {
//					ArrayList<EDMOrganization> list = new ArrayList<>(facilitySelected.getEdmEntityIdByOwner().getOrganizationsByMetaId());
//					list.sort(EDMUtil::compareEntityVersion);
//					EDMOrganization edmDataproductRelatedOrganization = list.get(0);
//					if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
//						facetsDataProviders.add(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
//					}
//				}
//
//			}

			List<String> categoryList = facilitySelected.getCategory().stream()
					.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.map(Category::getUid)
					.filter(uid -> uid.contains("category:"))
					.collect(Collectors.toList());

			discoveryList.add(new DiscoveryItemBuilder(facilitySelected.getMetaId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId(),
					null)
					.uid(facility.getUid())
					.title(facility.getTitle())
					.description(facility.getDescription())
					.availableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat("application/epos.geo+json")
							.format("application/epos.geo+json")
							.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_EQUIPMENTS + "all"+ API_FORMAT + "application/epos.geo+json"+"&facilityid=" + facilitySelected.getMetaId())
							.label("GEOJSON")
							.description(AvailableFormatType.CONVERTED)
							.build()))
					//TODO: .setFacilityProvider(facetsDataProviders)
					.setCategories(categoryList.isEmpty() ? null : categoryList)
					.build());

			FacetsNodeTree categories = FacetsGeneration.generateResponseUsingCategories(discoveryList);
			categories.getNodes().forEach(node -> node.setDistributions(null));
			facility.setCategories(categories.getFacets());

			return facility;
		}
	}

	public static FeaturesCollection generateAsGeoJson(org.epos.eposdatamodel.Facility facilitySelected, String equipmenttypes) {

		List<Category> categoriesFromDB = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll().stream().filter(item -> ((org.epos.eposdatamodel.Category) item).getStatus().equals(StatusType.PUBLISHED)).collect(Collectors.toList());

		FeaturesCollection geojson = new FeaturesCollection();

		Feature feature = new Feature();

		//COMMON FOR FACILITY
		feature.addSimpleProperty("Name", Optional.ofNullable(facilitySelected.getTitle()).orElse(""));
		feature.addSimpleProperty("Description", Optional.ofNullable(facilitySelected.getDescription()).orElse(""));
		feature.addSimpleProperty("Type", Optional.ofNullable(categoriesFromDB
				.stream()
				.filter(cat -> cat.getUid().equals(facilitySelected.getType())).map(Category::getName).collect(Collectors.toList())).get().toString());

		for(LinkedEntity locLe : facilitySelected.getSpatialExtent()) {
			Location loc = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(locLe);
			String location = loc.getLocation();
			boolean isPoint = location.contains("POINT");
			location = location.replaceAll("POLYGON", "").replaceAll("POINT", "").replaceAll("\\(", "").replaceAll("\\)", "");
			String[] coordinates = location.split("\\,");
			Geometry geometry = null;

			if(isPoint) {
				geometry = new Point();
				for(String coo : coordinates) {
					String[] cooz = coo.split(" ");
					if(cooz.length==2) {
						((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
					}else {
						((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
					}
				}
			}else {
				geometry = new Polygon();
				ArrayList<PointCoordinates> deep = new ArrayList<PointCoordinates>();
				for(String coo : coordinates) {
					String[] cooz = coo.split(" ");
					if(cooz.length==2) {
						deep.add(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
					}else {
						deep.add(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
					}
				}
				((Polygon) geometry).setStartingPoint(deep.get(0));
				for(int i = 1; i<deep.size();i++)
					((Polygon) geometry).addAdditionalPoint(deep.get(i));	
			}
			feature.setGeometry(geometry);
		}

		LinkObject link = new LinkObject();

		link.setAuthenticatedDownload(false);
		link.setLabel("Equipments");
		link.setType("application/epos.table.geo+json");
		link.setHref(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_EQUIPMENTS + "all"+ API_FORMAT + "application/epos.geo+json"+"&facilityid=" + facilitySelected.getMetaId());

		feature.addSimpleProperty("@epos_links",List.of(link));



		geojson.addFeature(feature);

		return geojson;
	}


}