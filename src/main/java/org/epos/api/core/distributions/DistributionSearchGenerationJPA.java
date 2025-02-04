package org.epos.api.core.distributions;

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
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.*;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;


public class DistributionSearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionSearchGenerationJPA.class);
	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/resources/details/";
	private static final String PARAMETER__SCIENCE_DOMAIN = "sciencedomains";
	private static final String PARAMETER__SERVICE_TYPE = "servicetypes";

	public static SearchResponse generate(Map<String,Object> parameters, User user) {

		LOGGER.info("Requests start - JPA method");

		long startTime = System.currentTimeMillis();

		List<StatusType> versions = new ArrayList<>();

		if(parameters.containsKey("versioningStatus")){
			Arrays.stream(parameters.get("versioningStatus").toString().split(",")).forEach(version -> {
				versions.add(StatusType.valueOf(version));
			});
		} else {
			versions.add(StatusType.PUBLISHED);
		}

		List<DataProduct> dataproducts  = new ArrayList<>();
		// Retrieve all needed information available (if status in status list and
		if(versions.contains(StatusType.PUBLISHED)){
			dataproducts.addAll(DatabaseConnections.getInstance().getDataproducts().stream().filter(Objects::nonNull).filter(item -> item.getStatus().equals(StatusType.PUBLISHED)).collect(Collectors.toList()));
		}

		for(DataProduct dataProduct : DatabaseConnections.getInstance().getDataproducts()) {
			if(Objects.nonNull(dataProduct)){
				if(dataProduct.getEditorId().equals(user.getAuthIdentifier())){
					System.out.println("[VersioningStatus] Found dataproduct "+dataProduct.getEditorId()+" "+user.getAuthIdentifier()+" "+dataProduct.getStatus()+" "+dataProduct.getInstanceId());
					if(versions.contains(dataProduct.getStatus())){
						dataproducts.add(dataProduct);
					}
				}
			}
		}
		//dataproducts.addAll(DatabaseConnections.getInstance().getDataproducts().stream().filter(Objects::nonNull).filter(item -> (versions.contains(item.getStatus()) && !item.getStatus().equals(StatusType.PUBLISHED) && item.getEditorId().equals(finalUser.getAuthIdentifier()))).collect(Collectors.toList()));
		List<Organization> organizationList = DatabaseConnections.getInstance().getOrganizationList();
		List<Category> categoryList1 = DatabaseConnections.getInstance().getCategoryList();


		LOGGER.info("Apply filter using input parameters: {}", parameters.toString());
		// Apply filtering
		dataproducts = DistributionFilterSearch.doFilters(dataproducts, parameters);

		Map<String, DiscoveryItem> discoveryMap = new HashMap<String, DiscoveryItem>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<Category> scienceDomains = new HashSet<>();
		Set<Category> serviceTypes = new HashSet<>();
		Set<Organization> organizationsEntityIds = new HashSet<>();

		dataproducts.forEach(dataproduct -> {

			Set<String> facetsDataProviders = new HashSet<>();
			List<String> categoryList = new ArrayList<>();

			// DATA PRODUCT
			if(dataproduct.getCategory()!=null){
				dataproduct.getCategory()
						.forEach(linkedEntity -> {
							Optional<Category> category = categoryList1.stream().filter(categoryFromList -> categoryFromList.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
							if(category.isPresent()) {
								if(category.get().getUid().contains("category:")) categoryList.add(category.get().getUid());
								else {
									scienceDomains.add(category.get());
								}
							}
						});
			}

			if(dataproduct.getPublisher()!=null){
				dataproduct.getPublisher()
						.forEach(linkedEntity -> {
							Optional<Organization> organization = organizationList.stream().filter(organizationFromList -> organizationFromList.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
							if(organization.isPresent()) {
								if(organization.get().getLegalName()!=null) facetsDataProviders.add(String.join(",",organization.get().getLegalName()));
								organizationsEntityIds.add(organization.get());
							}
						});
			}

			// Keywords
			keywords.addAll(Arrays.stream(Optional.ofNullable(dataproduct.getKeywords())
							.orElse("").split(",\t"))
					.map(String::toLowerCase)
					.map(String::trim)
					.collect(Collectors.toSet()));

			if(dataproduct.getDistribution()!=null)
				dataproduct.getDistribution().forEach(linkedEntity -> {
							Optional<Distribution> distribution = DatabaseConnections.getInstance().getDistributionList().stream().filter(distributionFromList -> distributionFromList.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
							if(distribution.isPresent()) {

								Set<String> facetsServiceProviders = new HashSet<>();

								// AVAILABLE FORMATS
								List<AvailableFormat> availableFormats = AvailableFormatsGeneration.generate(distribution.get());

								if(distribution.get().getAccessService()!=null){
									distribution.get().getAccessService().forEach(linkedEntity1 -> {
										Optional<WebService> webService = DatabaseConnections.getInstance().getWebServiceList().stream().filter(webserviceFromList -> webserviceFromList.getInstanceId().equals(linkedEntity1.getInstanceId())).findFirst();
										if(webService.isPresent()) {
											if(webService.get().getProvider()!=null){
												Optional<Organization> organization = organizationList.stream().filter(organizationFromList -> organizationFromList.getInstanceId().equals(webService.get().getProvider().getInstanceId())).findFirst();
												if(organization.isPresent()) {
													if(organization.get().getLegalName()!=null) facetsServiceProviders.add(String.join(",",organization.get().getLegalName()));
													organizationsEntityIds.add(organization.get());
												}
											}


											// Service Types
											if(webService.get().getCategory()!=null){
												webService.get().getCategory()
														.forEach(linkedEntity2 -> {
															Optional<Category> category = categoryList1.stream().filter(categoryFromList -> categoryFromList.getInstanceId().equals(linkedEntity2.getInstanceId())).findFirst();
															category.ifPresent(serviceTypes::add);
														});
											}
										}
									});
								}

								DiscoveryItem discoveryItem = new DiscoveryItemBuilder(distribution.get().getInstanceId(),
										EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.get().getInstanceId(),
										EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.get().getInstanceId()+"?extended=true")
										.uid(distribution.get().getUid())
										.title(distribution.get().getTitle()!=null?String.join(";",distribution.get().getTitle()):null)
										.description(distribution.get().getDescription()!=null? String.join(";",distribution.get().getDescription()):null)
										.availableFormats(availableFormats)
										.setSha256id(distribution.get().getUid()!=null? DigestUtils.sha256Hex(distribution.get().getUid()) : "")
										.setDataprovider(facetsDataProviders)
										.setVersioningStatus(dataproduct.getStatus().name())
										.setServiceProvider(facetsServiceProviders)
										.setCategories(categoryList.isEmpty() ? null : categoryList)
										.build();

								if (EnvironmentVariables.MONITORING != null && EnvironmentVariables.MONITORING.equals("true")) {
									discoveryItem.setStatus(ZabbixExecutor.getInstance().getStatusInfoFromSha(discoveryItem.getSha256id()));
									discoveryItem.setStatusTimestamp(ZabbixExecutor.getInstance().getStatusTimestampInfoFromSha(discoveryItem.getSha256id()));
								}

								discoveryMap.put(discoveryItem.getSha256id(), discoveryItem);
							}
						});
		});

		LOGGER.info("Final number of results: {}", discoveryMap.size());

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

		LOGGER.info("Number of organizations retrieved {}", organizationsEntityIds.size());

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

		List<DataServiceProvider> collection = DataServiceProviderGeneration.getProviders(new ArrayList<>(organizationsEntityIds));

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

		LOGGER.info("Result done in ms: {}", duration);

		return response;

	}

}