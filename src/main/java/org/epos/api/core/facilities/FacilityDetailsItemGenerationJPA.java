package org.epos.api.core.facilities;

import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.Facility;
import org.epos.api.beans.SpatialInformation;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.dbapi.util.EDMUtil;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.epos.library.objects.LinkObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class FacilityDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityDetailsItemGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/facilities/details/";
	private static final String API_PATH_EXECUTE_EQUIPMENTS  = EnvironmentVariables.API_CONTEXT+"/equipments/";
	private static final String API_FORMAT = "?format=";

	public static Object generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		EntityManager em = new DBService().getEntityManager();

		List<EDMFacility> facilitySelectedList = getFromDB(em, EDMFacility.class,
				"facility.findAllByMetaId",
				"METAID", parameters.get("id"));

		if (facilitySelectedList.stream().noneMatch(facSelected -> facSelected.getState().equals("PUBLISHED")))
			return null;

		EDMFacility facilitySelected = facilitySelectedList.stream().filter(facSelected -> facSelected.getState().equals("PUBLISHED")).collect(Collectors.toList()).get(0);

		if (facilitySelected == null) return null;

		if(parameters.containsKey("format") && parameters.get("format").toString().equals("application/epos.geo+json"))
			return generateAsGeoJson(facilitySelectedList.get(0));
		else {
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

			facility.setAvailableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat("application/epos.geo+json")
					.format("application/epos.geo+json")
					.href(EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId() + API_FORMAT + "application/epos.geo+json")
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


			discoveryList.add(new DiscoveryItemBuilder(facilitySelected.getMetaId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId())
					.uid(facility.getUid())
					.title(facility.getTitle())
					.description(facility.getDescription())
					.availableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat("application/epos.geo+json")
							.format("application/epos.geo+json")
							.href(EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId() + API_FORMAT + "application/epos.geo+json")
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

	public static FeaturesCollection generateAsGeoJson(EDMFacility facilitySelected) {

		EntityManager em = new DBService().getEntityManager();
		
		List<EDMCategory> categoriesFromDB = getFromDB(em, EDMCategory.class, "EDMCategory.findAll");
		em.close();
		
		FeaturesCollection geojson = new FeaturesCollection();

		Feature feature = new Feature();

		//COMMON FOR FACILITY
		feature.addSimpleProperty("Name", Optional.ofNullable(facilitySelected.getTitle()).orElse(""));
		feature.addSimpleProperty("Description", Optional.ofNullable(facilitySelected.getDescription()).orElse(""));
		feature.addSimpleProperty("Type", Optional.ofNullable(categoriesFromDB
				.stream()
				.filter(cat -> cat.getUid().equals(facilitySelected.getType())).map(EDMCategory::getName).collect(Collectors.toList())).get().toString());

		for(EDMFacilitySpatial loc : facilitySelected.getFacilitySpatialsByInstanceId()) {
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