package org.epos.api.core.distributions;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import model.StatusType;
import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.*;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.filtersearch.DistributionFilterSearch;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.Node;
import org.epos.eposdatamodel.*;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class DistributionSearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionSearchGenerationJPA.class);
	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/resources/details/";
	private static final String PARAMETER__SCIENCE_DOMAIN = "sciencedomains";
	private static final String PARAMETER__SERVICE_TYPE = "servicetypes";

	public static SearchResponse generate(Map<String,Object> parameters) {

		LOGGER.info("Requests start - JPA method");

		long startTime = System.currentTimeMillis();

		// Retrieve all Dataproducts available
		List<DataProduct> dataproducts  = (List<DataProduct>) AbstractAPI.retrieveAPI(EntityNames.DATAPRODUCT.name()).retrieveAll().parallelStream().filter(item -> ((org.epos.eposdatamodel.DataProduct) item).getStatus().equals(StatusType.PUBLISHED)).collect(Collectors.toList());
		List<SoftwareApplication> softwareApplications = AbstractAPI.retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll();

		LOGGER.info("Apply filter using input parameters: "+parameters.toString());
		// Apply filtering
		dataproducts = DistributionFilterSearch.doFilters(dataproducts, parameters);

		Map<String, DiscoveryItem> discoveryMap = new HashMap<String, DiscoveryItem>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<Category> scienceDomains = new HashSet<>();
		Set<Category> serviceTypes = new HashSet<>();
		Set<Organization> organizationsEntityIds = new HashSet<>();

		dataproducts.parallelStream().forEach(dataproduct -> {

			Set<String> facetsDataProviders = new HashSet<>();
			List<String> categoryList = new ArrayList<>();

			// DATA PRODUCT
			if(dataproduct.getCategory()!=null)
				categoryList = dataproduct.getCategory().parallelStream()
						.map(LinkedEntity::getUid)
						.filter(uid -> uid.contains("category:"))
						.collect(Collectors.toList());

			if(dataproduct.getPublisher()!=null)
				dataproduct.getPublisher().parallelStream()
						.map(linkedEntity -> (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
						.filter(Objects::nonNull)
						.map(Organization::getLegalName)
						.collect(Collectors.toList())
						.parallelStream()
						.forEach(facetsDataProviders::addAll);

			List<String> finalCategoryList = categoryList;

			Optional.ofNullable(dataproduct.getDistribution())
			.ifPresent(isDistribution -> isDistribution.parallelStream()
					.forEach(distributionLe -> {

						Distribution distribution = (Distribution) LinkedEntityAPI.retrieveFromLinkedEntity(distributionLe);

						Set<String> facetsServiceProviders = new HashSet<>();

						List<AvailableFormat> availableFormats = new ArrayList<>();

						System.out.println(dataproduct.getPublisher());
						if(dataproduct.getPublisher()!=null){
							for(Organization organization : dataproduct.getPublisher().parallelStream()
									.map(linkedEntity -> (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity)).collect(Collectors.toList())){
								facetsDataProviders.add(String.join(",",organization.getLegalName()));
								organizationsEntityIds.add(organization);
							}
						}
						if(distribution.getAccessService()!=null){
							for(WebService webService : distribution.getAccessService().parallelStream().map(linkedEntity -> (WebService) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity)).collect(Collectors.toList())){
								availableFormats.addAll(AvailableFormatsGeneration.generate(distribution, webService, softwareApplications));

								if(webService.getProvider()!=null){
									Organization organization = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(webService.getProvider());
									facetsServiceProviders.add(String.join(",",organization.getLegalName()));
									organizationsEntityIds.add(organization);
								}
								// Service Types
								if(webService.getCategory()!=null){
									webService.getCategory()
											.parallelStream()
											.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
											.filter(Objects::nonNull)
											.forEach(serviceTypes::add);
								}
							}
						}

						DiscoveryItem discoveryItem = new DiscoveryItemBuilder(distribution.getInstanceId(),
								EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId(),
								EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId()+"?extended=true")
								.uid(distribution.getUid())
								.title(String.join(";",distribution.getTitle()))
								.description(String.join(";",distribution.getDescription()))
								.availableFormats(availableFormats)
								.setSha256id(DigestUtils.sha256Hex(distribution.getUid()))
								.setDataprovider(facetsDataProviders)
								.setServiceProvider(facetsServiceProviders)
								.setCategories(finalCategoryList.isEmpty() ? null : finalCategoryList)
								.build();

						System.out.println(discoveryItem);

						if (EnvironmentVariables.MONITORING != null && EnvironmentVariables.MONITORING.equals("true")) {
							discoveryItem.setStatus(ZabbixExecutor.getInstance().getStatusInfoFromSha(discoveryItem.getSha256id()));
							discoveryItem.setStatusTimestamp(ZabbixExecutor.getInstance().getStatusTimestampInfoFromSha(discoveryItem.getSha256id()));
						}

						discoveryMap.put(discoveryItem.getSha256id(), discoveryItem);
					})
					);

			// Keywords
			keywords.addAll(Arrays.stream(Optional.ofNullable(dataproduct.getKeywords())
					.orElse("").split(",\t"))
					.map(String::toLowerCase)
					.map(String::trim)
					.collect(Collectors.toSet()));

			// Science Domains
			scienceDomains.addAll(Optional.ofNullable(dataproduct.getCategory())
					.orElse(Collections.emptyList())
					.parallelStream()
					.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
					.filter(Objects::nonNull)
					.collect(Collectors.toSet()));
		});

		LOGGER.info("Final number of results: "+discoveryMap.values().size());

		Node results = new Node("results");
		if(parameters.containsKey("facets") && parameters.get("facets").toString().equals("true")) {
			switch(parameters.get("facetstype").toString()) {
			case "categories":
				results.addChild(FacetsGeneration.generateResponseUsingCategories(discoveryMap.values()).getFacets());
				break;
			case "dataproviders":
				results.addChild(FacetsGeneration.generateResponseUsingDataproviders(discoveryMap.values()).getFacets());
				break;
			case "serviceproviders":
				results.addChild(FacetsGeneration.generateResponseUsingServiceproviders(discoveryMap.values()).getFacets());
				break;
			default:
				break;
			}

		}else {
			Node child = new Node();
			child.setDistributions(discoveryMap.values());
			results.addChild(child);
		}

		LOGGER.info("Number of organizations retrieved "+organizationsEntityIds.size());

		List<String> keywordsCollection = keywords.parallelStream()
				.filter(Objects::nonNull)
				.sorted()
				.collect(Collectors.toList());

		NodeFilters keywordsNodes = new NodeFilters("keywords");

		keywordsCollection.forEach(r -> {
			NodeFilters node = new NodeFilters(r);
			node.setId(Base64.getEncoder().encodeToString(r.getBytes()));
			keywordsNodes.addChild(node);
		});

		/*List<EDMOrganization> organizationsCollection = organizations.parallelStream()
				.sorted(Comparator.comparing(o -> o.getOrganizationLegalnameByInstanceId().parallelStream()
						.map(EDMOrganizationLegalname::getLegalname)
						.findFirst()
						.orElse("")))
				.collect(Collectors.toList());*/

		List<DataServiceProvider> collection = DataServiceProviderGeneration.getProviders(new ArrayList<Organization>(organizationsEntityIds));

		NodeFilters organisationsNodes = new NodeFilters("organisations");

		collection.forEach(resource->{
			NodeFilters node = new NodeFilters(resource.getDataProviderLegalName());
			node.setId(resource.getInstanceid());
			organisationsNodes.addChild(node);

			resource.getRelatedDataProvider().forEach(relatedData ->{
				NodeFilters relatedNodeDataProvider = new NodeFilters(relatedData.getDataProviderLegalName());
				relatedNodeDataProvider.setId(relatedData.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataProvider);
			});
			resource.getRelatedDataServiceProvider().forEach(relatedDataService ->{
				NodeFilters relatedNodeDataServiceProvider = new NodeFilters(relatedDataService.getDataProviderLegalName());
				relatedNodeDataServiceProvider.setId(relatedDataService.getInstanceid());
				organisationsNodes.addChild(relatedNodeDataServiceProvider);
			});
		});

		NodeFilters scienceDomainsNodes = new NodeFilters(PARAMETER__SCIENCE_DOMAIN);
		scienceDomains.forEach(r ->
		scienceDomainsNodes.addChild(new NodeFilters(r.getInstanceId(), r.getName()))
				);

		NodeFilters serviceTypesNodes = new NodeFilters(PARAMETER__SERVICE_TYPE);
		serviceTypes.forEach(r ->
		serviceTypesNodes.addChild(new NodeFilters(r.getInstanceId(), r.getName()))
				);

		ArrayList<NodeFilters> filters = new ArrayList<NodeFilters>();
		filters.add(keywordsNodes);
		filters.add(organisationsNodes);
		filters.add(scienceDomainsNodes);
		filters.add(serviceTypesNodes);

		SearchResponse response = new SearchResponse(results, filters);

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		LOGGER.info("Result done in ms: "+duration);

		return response;

	}

}
