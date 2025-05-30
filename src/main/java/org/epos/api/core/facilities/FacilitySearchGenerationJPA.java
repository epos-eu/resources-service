package org.epos.api.core.facilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.beans.NodeFilters;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.filtersearch.FacilityFilterSearch;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.facets.Facets;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.Node;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.Equipment;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import model.StatusType;

public class FacilitySearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilitySearchGenerationJPA.class);

	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/facilities/details/";
	private static final String API_PATH_EXECUTE_EQUIPMENTS = EnvironmentVariables.API_CONTEXT + "/equipments/";
	private static final String API_FORMAT = "?format=";

	private static final String PARAMETER_FACILITY_TYPES = "facilitytypes";
	private static final String PARAMETER_EQUIPMENT_TYPES = "equipmenttypes";

	public static SearchResponse generate(Map<String, Object> parameters) {

		LOGGER.info("Requests start - JPA method");

		long startTime = System.currentTimeMillis();

		List<Facility> facilities = DatabaseConnections.getInstance().getFacilityList();
		List<Organization> organizationForOwners = DatabaseConnections.getInstance().getOrganizationList();
		List<Category> categoriesFromDB = DatabaseConnections.getInstance().getCategoryList();

		LOGGER.info("Apply filter using input parameters: " + parameters.toString());
		// TODO for facility
		facilities = FacilityFilterSearch.doFilters(facilities, parameters, categoriesFromDB, organizationForOwners);

		Set<DiscoveryItem> discoveryMap = new HashSet<DiscoveryItem>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<Category> facilityTypes = new HashSet<>();
		Set<Category> equipmentTypes = new HashSet<>();

		List<DataProduct> dataProducts = DatabaseConnections.getInstance().getDataproducts().stream()
				.filter(d -> d.getStatus().equals(StatusType.PUBLISHED))
				.collect(Collectors.toList());

		// get all the distributions that have a category that is a facility category
		for (var dataProduct : dataProducts) {
			if (dataProduct.getCategory() == null || dataProduct.getCategory().isEmpty()) {
				continue;
			}
			// for each category of this data product
			for (var linkedEntity : dataProduct.getCategory()) {
				Optional<Category> category = categoriesFromDB.stream()
						.filter(categoryFromList -> categoryFromList.getInstanceId()
								.equals(linkedEntity.getInstanceId()))
						.findFirst();
				if (category.isEmpty() || !Facets.getCategoryType(category.get()).equals(Facets.Type.FACILITY)) {
					continue;
				}

				// if it is a facility category
				// get the distributions and add them to the discovery list
				for (var le : dataProduct.getDistribution()) {
					Optional<Distribution> distribution = DatabaseConnections.getInstance()
							.getDistributionList().stream()
							.filter(dist -> dist.getInstanceId().equals(le.getInstanceId()))
							.findFirst();
					if (distribution.isEmpty()) {
						continue;
					}

					DiscoveryItem discoveryItem = new DiscoveryItemBuilder(
							distribution.get().getInstanceId(),
							EnvironmentVariables.API_HOST + API_PATH_DETAILS
									+ distribution.get().getInstanceId(),
							EnvironmentVariables.API_HOST + API_PATH_DETAILS
									+ distribution.get().getInstanceId()
									+ "?extended=true")
							.uid(distribution.get().getUid())
							.metaId(distribution.get().getMetaId())
							.title(distribution.get().getTitle() != null
									? String.join(";", distribution.get().getTitle())
									: null)
							.description(distribution.get().getDescription() != null
									? String.join(";", distribution.get().getDescription())
									: null)
							.availableFormats(AvailableFormatsGeneration.generate(distribution.get()))
							// .dataProvider(facetsDataProviders)
							// .serviceProvider(facetsServiceProviders)
							.categories(Arrays.asList(category.get().getUid()))
							.build();
					discoveryMap.add(discoveryItem);
				}
			}
		}

		for (Facility facility : facilities) {
			Set<String> facetsFacilityProviders = new HashSet<>();

			List<String> categoryList = facility.getCategory().stream()
					.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.map(Category::getUid)
					.filter(uid -> uid.contains("category:"))
					.collect(Collectors.toList());

			for (Organization organization : organizationForOwners) {
				if (organization.getOwns() == null) {
					continue;
				}
				for (LinkedEntity linkedEntity : organization.getOwns()) {
					if (linkedEntity.getEntityType().equals(EntityNames.FACILITY.name())
							&& linkedEntity.getInstanceId().equals(facility.getInstanceId())) {
						facetsFacilityProviders.add(linkedEntity.getInstanceId());
					}
				}
			}

			DiscoveryItem discoveryItem = new DiscoveryItemBuilder(facility.getInstanceId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + facility.getInstanceId(),
					null)
					.uid(facility.getUid())
					.title(facility.getTitle())
					.description(facility.getDescription())
					.sha256id(DigestUtils.sha256Hex(facility.getUid()))
					.availableFormats(List.of(new AvailableFormat.AvailableFormatBuilder()
							.originalFormat("application/epos.geo+json")
							.format("application/epos.geo+json")
							.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE_EQUIPMENTS + "all" + API_FORMAT
									+ "application/epos.geo+json" + "&facilityid=" + facility.getInstanceId())
							.label("GEOJSON")
							.type(AvailableFormatType.CONVERTED)
							.build()))
					.facilityProvider(facetsFacilityProviders)
					.categories(categoryList.isEmpty() ? null : categoryList)
					.build();

			keywords.addAll(Arrays.stream(Optional.ofNullable(facility.getKeywords()).orElse("").split(",\t"))
					.map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));

			// Facility Types
			categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(facility.getType()))
					.forEach(facilityTypes::add);

			// Equipment types
			for (Equipment equipment : DatabaseConnections.getInstance().getEquipmentList()) {
				if (equipment.getIsPartOf() == null) {
					continue;
				}
				for (LinkedEntity linkedEntity : equipment.getIsPartOf()) {
					if (linkedEntity.getEntityType().equals(EntityNames.FACILITY.name())
							&& linkedEntity.getInstanceId().equals(facility.getInstanceId())) {
						categoriesFromDB
								.stream()
								.filter(cat -> cat.getUid().equals(equipment.getType()))
								.forEach(equipmentTypes::add);
					}
				}
			}

			discoveryMap.add(discoveryItem);
		}

		LOGGER.info("Final number of results: " + discoveryMap.size());

		Node results = new Node("results");
		if (parameters.containsKey("facets") && parameters.get("facets").toString().equals("true")) {
			switch (parameters.get("facetstype").toString()) {
				case "categories":
					var facets = FacetsGeneration.generateResponseUsingCategories(discoveryMap, Facets.Type.FACILITY)
							.getFacets();
					results.addChild(facets);
					break;
				case "facilityproviders": // TODO: facility providers
					results.addChild(FacetsGeneration.generateResponseUsingDataproviders(discoveryMap).getFacets());
					break;
				default:
					break;
			}
		} else {
			Node child = new Node();
			child.setDistributions(discoveryMap);
			results.addChild(child);
		}

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

		// TODO: right now the organizations are never added. See if this is needed
		List<DataServiceProvider> collection = DataServiceProviderGeneration
				.getProviders(new ArrayList<>());
		NodeFilters organisationsNodes = new NodeFilters("organisations");
		collection.forEach(resource -> {
			NodeFilters node = new NodeFilters(resource.getDataProviderLegalName());
			node.setId(resource.getInstanceid());
			organisationsNodes.addChild(node);

			resource.getRelatedDataProvider().forEach(relatedData -> {
				NodeFilters relatedNodeDataProvider = new NodeFilters(relatedData.getDataProviderLegalName());
				relatedNodeDataProvider.setId(relatedData.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataProvider);
			});

			resource.getRelatedDataServiceProvider().forEach(relatedDataService -> {
				NodeFilters relatedNodeDataServiceProvider = new NodeFilters(
						relatedDataService.getDataProviderLegalName());
				relatedNodeDataServiceProvider.setId(relatedDataService.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataServiceProvider);
			});
		});

		NodeFilters facilityTypesNodes = new NodeFilters(PARAMETER_FACILITY_TYPES);
		facilityTypes.forEach(r -> facilityTypesNodes.addChild(new NodeFilters(r.getInstanceId(), r.getName())));

		NodeFilters equipmentTypesNodes = new NodeFilters(PARAMETER_EQUIPMENT_TYPES);
		equipmentTypes.forEach(r -> equipmentTypesNodes.addChild(new NodeFilters(r.getInstanceId(), r.getName())));

		ArrayList<NodeFilters> filters = new ArrayList<NodeFilters>();
		filters.add(keywordsNodes);
		filters.add(organisationsNodes);
		filters.add(facilityTypesNodes);
		filters.add(equipmentTypesNodes);

		SearchResponse response = new SearchResponse(results, filters);

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		LOGGER.info("Result done in ms: " + duration);

		return response;
	}
}
