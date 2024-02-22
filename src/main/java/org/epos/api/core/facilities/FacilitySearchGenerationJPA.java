package org.epos.api.core.facilities;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.SearchResponse;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.filtersearch.FacilityFilterSearch;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.beans.NodeFilters;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.Node;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class FacilitySearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilitySearchGenerationJPA.class);

	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/facilities/details/";
	private static final String API_PATH_EXECUTE_EQUIPMENTS  = EnvironmentVariables.API_CONTEXT+"/equipments/";
	private static final String API_FORMAT = "?format=";

	private static final String PARAMETER_FACILITY_TYPES = "facilitytypes";
	private static final String PARAMETER_EQUIPMENT_TYPES = "equipmenttypes";

	//@Valid String q, @Valid String bbox,
	//@Valid String keywords, @Valid String facilitytypes, @Valid String equipmenttypes,
	//@Valid String organisations, @Valid String facetsType, @Valid Boolean facets) {

	public static SearchResponse generate(Map<String,Object> parameters) {

		LOGGER.info("Requests start - JPA method");
		EntityManager em = new DBService().getEntityManager();

		long startTime = System.currentTimeMillis();

		List<EDMFacility> facilities  = getFromDB(em, EDMFacility.class, "facility.findAllByState", "STATE", "PUBLISHED");
		List<EDMOrganization> organizationForOwners  = getFromDB(em, EDMOrganization.class, "organization.findAllByState", "STATE", "PUBLISHED");
		List<EDMCategory> categoriesFromDB = getFromDB(em, EDMCategory.class, "EDMCategory.findAll");


		LOGGER.info("Apply filter using input parameters: "+parameters.toString());
		facilities = FacilityFilterSearch.doFilters(facilities, parameters, categoriesFromDB, organizationForOwners); //TODO for facility

		Map<String, DiscoveryItem> discoveryMap = new HashMap<String, DiscoveryItem>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<EDMCategory> facilityTypes = new HashSet<>();
		Set<EDMCategory> equipmentTypes = new HashSet<>();
		Set<EDMEdmEntityId> organizationsEntityIds = new HashSet<>();

		facilities.stream().forEach(facility -> {

			Set<String> facetsFacilityProviders = new HashSet<>();

			List<String> categoryList = facility.getFacilityCategoriesByInstanceId().stream()
					.map(EDMFacilityCategory::getCategoryByCategoryId)
					.filter(Objects::nonNull)
					.filter(e -> e.getUid().contains("category:"))
					.map(EDMCategory::getUid)
					.collect(Collectors.toList());


			if(facility.getEquipmentFacilitiesByInstanceId()!=null) {
				for(EDMEquipmentFacility item : facility.getEquipmentFacilitiesByInstanceId()) {
					EDMEquipment own = item.getEquipmentByInstanceEquipmentId();
					
					
					organizationForOwners.stream()
					.map(EDMOrganization::getOwnsByInstanceId)
					.filter(Objects::nonNull)
					.forEach(organizationowner->{
						organizationowner.stream()
						.filter(edmEntity -> edmEntity.getEntityMetaId().equals(own.getMetaId()))
						.map(EDMOrganizationOwner::getOrganizationByInstanceOrganizationId)
						.map(EDMOrganization::getOrganizationLegalnameByInstanceId)
						.flatMap(Collection::stream)
						.filter(legalname -> Objects.nonNull(legalname))
						.map(EDMOrganizationLegalname::getLegalname)
						.forEach(facetsFacilityProviders::add);
						
						organizationowner.stream()
						.filter(edmEntity -> edmEntity.getEntityMetaId().equals(own.getMetaId()))
						.map(EDMOrganizationOwner::getOrganizationByInstanceOrganizationId)
						.map(EDMOrganization::getEdmEntityIdByMetaId)
						.filter(Objects::nonNull)
						.collect(Collectors.toList())
						.forEach(organizationsEntityIds::add);
						
					});
				}
			}

			DiscoveryItem discoveryItem = new DiscoveryItemBuilder(facility.getMetaId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + facility.getMetaId())
					.uid(facility.getUid())
					.title(facility.getTitle())
					.description(facility.getDescription())
					.setSha256id(DigestUtils.sha256Hex(facility.getUid()))
					.availableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat("application/epos.geo+json")
							.format("application/epos.geo+json")
							.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_EQUIPMENTS + "all"+ API_FORMAT + "application/epos.geo+json"+"&facilityid=" + facility.getMetaId() + (parameters.containsKey("equipmenttypes")? "&equipmenttypes="+parameters.get("equipmenttypes") : ""))
							.label("GEOJSON")
							.description(AvailableFormatType.CONVERTED)
							.build()))
					.setFacilityProvider(facetsFacilityProviders)
					.setCategories(categoryList.isEmpty() ? null : categoryList)
					.build();

			// Keywords
			Collection<EDMFacilityService> ws = facility.getFacilityServicesByInstanceId() != null ? facility.getFacilityServicesByInstanceId() : null;
			if (ws != null) {
				ws.forEach(instanceWS ->{
					keywords.addAll(Arrays.stream(Optional.ofNullable(instanceWS.getServiceByInstanceServiceId().getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
				});
			}

			// Facility Types
			categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(facility.getType()))
					.forEach(facilityTypes::add);
			

			//Equipment types
			if(facility.getEquipmentFacilitiesByInstanceId()!=null) {
				for(EDMEquipmentFacility item : facility.getEquipmentFacilitiesByInstanceId()) {
					categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(item.getEquipmentByInstanceEquipmentId().getType()))
					.forEach(equipmentTypes::add);
				}
			}

			discoveryMap.put(discoveryItem.getSha256id(), discoveryItem);
		});

		LOGGER.info("Final number of results: "+discoveryMap.values().size());

		Node results = new Node("results");
		if(parameters.containsKey("facets") && parameters.get("facets").toString().equals("true")) {
			switch(parameters.get("facetstype").toString()) {
			case "categories":
				results.addChild(FacetsGeneration.generateResponseUsingCategories(discoveryMap.values()).getFacets());
				break;
			case "facilityproviders":
				results.addChild(FacetsGeneration.generateResponseUsingDataproviders(discoveryMap.values()).getFacets());
				break; //TODO: facility providers
			default:
				break;
			}

		}else {
			Node child = new Node();
			child.setDistributions(discoveryMap.values());
			results.addChild(child);
		}

		LOGGER.info("Number of organizations retrieved "+organizationsEntityIds.size());

		List<String> keywordsCollection = keywords.stream()
				.filter(Objects::nonNull)
				.sorted()
				.collect(Collectors.toList());

		NodeFilters keywordsNodes = new NodeFilters("keywords");

		keywordsCollection.forEach(r -> {
			NodeFilters node = new NodeFilters(r);
			node.setId(Base64.getEncoder().encodeToString(r.getBytes()));
			keywordsNodes.addChild(node);
		});
		
		List<DataServiceProvider> collection = DataServiceProviderGeneration.getProviders(new ArrayList<EDMEdmEntityId>(organizationsEntityIds));

		NodeFilters organisationsNodes = new NodeFilters("organisations");

		collection.forEach(resource->{
			NodeFilters node = new NodeFilters(resource.getDataProviderLegalName());
			node.setId(resource.getInstanceid());
			organisationsNodes.addChild(node);

			//LOGGER.info("Number of related retrieved "+resource.getRelatedDataProvider().size());
			resource.getRelatedDataProvider().forEach(relatedData ->{
				NodeFilters relatedNodeDataProvider = new NodeFilters(relatedData.getDataProviderLegalName());
				relatedNodeDataProvider.setId(relatedData.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataProvider);
			});
			//LOGGER.info("Number of related retrieved "+resource.getRelatedDataServiceProvider().size());
			resource.getRelatedDataServiceProvider().forEach(relatedDataService ->{
				NodeFilters relatedNodeDataServiceProvider = new NodeFilters(relatedDataService.getDataProviderLegalName());
				relatedNodeDataServiceProvider.setId(relatedDataService.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataServiceProvider);
			});
		});

		NodeFilters facilityTypesNodes = new NodeFilters(PARAMETER_FACILITY_TYPES);
		facilityTypes.forEach(r ->
		facilityTypesNodes.addChild(new NodeFilters(r.getId(), r.getName()))
				);

		NodeFilters equipmentTypesNodes = new NodeFilters(PARAMETER_EQUIPMENT_TYPES);
		equipmentTypes.forEach(r ->
		equipmentTypesNodes.addChild(new NodeFilters(r.getId(), r.getName()))
				);

		ArrayList<NodeFilters> filters = new ArrayList<NodeFilters>();
		filters.add(keywordsNodes);
		filters.add(organisationsNodes);
		filters.add(facilityTypesNodes);
		filters.add(equipmentTypesNodes);

		SearchResponse response = new SearchResponse(results, filters);

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		LOGGER.info("Result done in ms: "+duration);

		return response;

	}

}
