package org.epos.api.core.distributions;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.SearchResponse;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.filtersearch.DistributionFilterSearch;
import org.epos.api.beans.NodeFilters;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.Node;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.dbapi.util.EDMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class DistributionSearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionSearchGenerationJPA.class);

	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/resources/details/";

	private static final String PARAMETER__SCIENCE_DOMAIN = "sciencedomains";
	private static final String PARAMETER__SERVICE_TYPE = "servicetypes";

	public static SearchResponse generate(Map<String,Object> parameters) {

		LOGGER.info("Requests start - JPA method");
		EntityManager em = new DBService().getEntityManager();

		long startTime = System.currentTimeMillis();

		List<EDMDataproduct> dataproducts  = getFromDB(em, EDMDataproduct.class, "dataproduct.findAllByState", "STATE", "PUBLISHED");


		LOGGER.info("Apply filter using input parameters: "+parameters.toString());
		dataproducts = DistributionFilterSearch.doFilters(dataproducts, parameters);

		Map<String, DiscoveryItem> discoveryMap = new HashMap<String, DiscoveryItem>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<EDMCategory> scienceDomains = new HashSet<>();
		Set<EDMCategory> serviceTypes = new HashSet<>();
		Set<EDMOrganization> organizations = new HashSet<>();
		Set<EDMEdmEntityId> organizationsEntityIds = new HashSet<>();

		dataproducts.stream().forEach(dataproduct -> {

			Set<String> facetsDataProviders = new HashSet<>();

			List<String> categoryList = dataproduct.getDataproductCategoriesByInstanceId().stream()
					.map(EDMDataproductCategory::getCategoryByCategoryId)
					.filter(Objects::nonNull)
					.filter(e -> e.getUid().contains("category:"))
					.map(EDMCategory::getUid)
					.collect(Collectors.toList());

			List<EDMWebservice> webservices = dataproduct.getIsDistributionsByInstanceId().stream()
					.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
					.filter(Objects::nonNull)
					.map(EDMDistribution::getWebserviceByAccessService)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			dataproduct.getPublishersByInstanceId().stream()
			.map(EDMPublisher::getEdmEntityIdByMetaOrganizationId)
			.filter(edmMetaId -> Objects.nonNull(edmMetaId.getOrganizationsByMetaId()) && !edmMetaId.getOrganizationsByMetaId().isEmpty())
			.map(edmMetaId -> edmMetaId.getOrganizationsByMetaId().stream().findFirst().orElse(null))
			.filter(Objects::nonNull)
			.map(EDMOrganization::getOrganizationLegalnameByInstanceId)
			.flatMap(Collection::stream)
			.filter(legalname -> Objects.nonNull(legalname))
			.map(EDMOrganizationLegalname::getLegalname)
			.forEach(facetsDataProviders::add);

			webservices.stream()
			.map(EDMWebservice::getEdmEntityIdByProvider)
			.filter(Objects::nonNull)
			.collect(Collectors.toList())
			.stream()
			.filter(edmMetaId -> Objects.nonNull(edmMetaId.getOrganizationsByMetaId()) && !edmMetaId.getOrganizationsByMetaId().isEmpty())
			.map(edmMetaId -> new ArrayList<>(edmMetaId.getOrganizationsByMetaId()))
			.map(list -> {
				list.sort(EDMUtil::compareEntityVersion);
				return list;
			})
			.map(list -> list.get(0))
			.filter(edmDataproductRelatedOrganization -> Objects.nonNull(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId()) && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty())
			.forEach(organizations::add);

			//dp.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())
			
			dataproduct.getPublishersByInstanceId().stream()
			.map(EDMPublisher::getEdmEntityIdByMetaOrganizationId)
			.filter(Objects::nonNull)
			.collect(Collectors.toList())
			.forEach(organizationsEntityIds::add);

			webservices.stream()
			.map(EDMWebservice::getEdmEntityIdByProvider)
			.filter(Objects::nonNull)
			.collect(Collectors.toList())
			.forEach(organizationsEntityIds::add);

			Optional.ofNullable(dataproduct.getIsDistributionsByInstanceId())
			.ifPresent(isDistribution -> isDistribution.stream()
					.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
					.filter(Objects::nonNull)
					.forEach(distribution -> {
						Set<String> facetsServiceProviders = new HashSet<>();

						dataproduct.getPublishersByInstanceId().stream()
						.map(EDMPublisher::getEdmEntityIdByMetaOrganizationId)
						.filter(edmMetaId -> Objects.nonNull(edmMetaId.getOrganizationsByMetaId()) && !edmMetaId.getOrganizationsByMetaId().isEmpty())
						.map(edmMetaId -> edmMetaId.getOrganizationsByMetaId().stream().findFirst().orElse(null))
						.filter(Objects::nonNull)
						.map(EDMOrganization::getOrganizationLegalnameByInstanceId)
						.flatMap(Collection::stream)
						.filter(legalname -> Objects.nonNull(legalname))
						.map(EDMOrganizationLegalname::getLegalname)
						.forEach(facetsDataProviders::add);

						if (Objects.nonNull(distribution.getWebserviceByAccessService()) &&
								Objects.nonNull(distribution.getWebserviceByAccessService().getEdmEntityIdByProvider())) {

							distribution.getWebserviceByAccessService()
							.getEdmEntityIdByProvider()
							.getOrganizationsByMetaId()
							.stream()
							.findFirst()
							.ifPresent(edmWebserviceRelatedOrganization ->
							edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId()
							.stream()
							.findFirst()
							.map(EDMOrganizationLegalname::getLegalname)
							.filter(Objects::nonNull)
							.ifPresent(facetsServiceProviders::add)
									);
						}

						DiscoveryItem discoveryItem = new DiscoveryItemBuilder(distribution.getMetaId(),
								EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getMetaId())
								.uid(distribution.getUid())
								.title(distribution.getDistributionTitlesByInstanceId()
										.stream()
										.findFirst()
										.map(EDMDistributionTitle::getTitle)
										.orElse(null))
								.description(distribution.getDistributionDescriptionsByInstanceId()
										.stream()
										.findFirst()
										.map(EDMDistributionDescription::getDescription)
										.orElse(null))
								.availableFormats(AvailableFormatsGeneration.generate(distribution))
								.setSha256id(DigestUtils.sha256Hex(distribution.getUid()))
								.setDataprovider(facetsDataProviders)
								.setServiceProvider(facetsServiceProviders)
								.setCategories(categoryList.isEmpty() ? null : categoryList)
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
			scienceDomains.addAll(Optional.ofNullable(dataproduct.getDataproductCategoriesByInstanceId())
					.orElse(Collections.emptyList())
					.stream()
					.map(EDMDataproductCategory::getCategoryByCategoryId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet()));

			// Service Types
			webservices.stream()
			.flatMap(webservice -> Optional.ofNullable(webservice.getWebserviceCategoriesByInstanceId())
					.orElse(Collections.emptyList())
					.stream()
					.map(EDMWebserviceCategory::getCategoryByCategoryId)
					.filter(Objects::nonNull))
			.forEach(serviceTypes::add);
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

		/*List<EDMOrganization> organizationsCollection = organizations.stream()
				.sorted(Comparator.comparing(o -> o.getOrganizationLegalnameByInstanceId().stream()
						.map(EDMOrganizationLegalname::getLegalname)
						.findFirst()
						.orElse("")))
				.collect(Collectors.toList());*/

		List<DataServiceProvider> collection = DataServiceProviderGeneration.getProviders(new ArrayList<EDMEdmEntityId>(organizationsEntityIds));

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
		scienceDomainsNodes.addChild(new NodeFilters(r.getId(), r.getName()))
				);

		NodeFilters serviceTypesNodes = new NodeFilters(PARAMETER__SERVICE_TYPE);
		serviceTypes.forEach(r ->
		serviceTypesNodes.addChild(new NodeFilters(r.getId(), r.getName()))
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
