package org.epos.api.core.software;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.api.beans.AvailableFormat;
import org.epos.api.beans.DiscoveryItem;
import org.epos.api.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.api.beans.NodeFilters;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.AvailableFormatsGeneration;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.enums.AvailableFormatType;
import org.epos.api.facets.Facets;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.Node;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.SoftwareApplication;
import org.epos.eposdatamodel.SoftwareSourceCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import model.StatusType;

public class SoftwareSearch {
	private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareSearch.class);
	private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/software/details/";
	private static final Pattern FORMAT_PATTERN = Pattern.compile("\\.([a-zA-Z0-9]+)(?:/|$|\\?)");

	public static SearchResponse generate(String query) {
		LOGGER.info("Generating discovery items with query {}", query);
		long startTime = System.currentTimeMillis();

		DataCollector dataCollector = new DataCollector();
		Set<DiscoveryItem> discoveryItems = new HashSet<>();
		Set<String> keywords = new HashSet<>();

		processDataProducts(query, dataCollector, discoveryItems);

		processSoftwareSourceCodes(query, dataCollector.softwareSourceCodes, discoveryItems, keywords);

		processSoftwareApplications(query, dataCollector.softwareApplications, discoveryItems, keywords);

		SearchResponse response = buildSearchResponse(discoveryItems, keywords);

		long duration = System.currentTimeMillis() - startTime;
		LOGGER.info("Result done in ms: " + duration);
		return response;
	}

	private static void processDataProducts(
			String query,
			DataCollector dataCollector,
			Set<DiscoveryItem> discoveryItems) {
		for (DataProduct dataProduct : dataCollector.dataProducts) {
			if (!hasValidCategory(dataProduct) || !matchesQuery(
					query,
					dataProduct.getTitle().getFirst(),
					dataProduct.getDescription().getFirst())) {
				continue;
			}

			for (var linkedEntity : dataProduct.getCategory()) {
				Optional<Category> category = findSoftwareCategory(
						linkedEntity.getInstanceId(),
						dataCollector.categories);
				if (category.isEmpty()) {
					continue;
				}

				addDistributionsToDiscovery(dataProduct, category.get(), discoveryItems);
			}
		}
	}

	private static void processSoftwareSourceCodes(
			String query,
			List<SoftwareSourceCode> softwareSourceCodes,
			Set<DiscoveryItem> discoveryItems,
			Set<String> keywords) {
		for (SoftwareSourceCode software : softwareSourceCodes) {
			if (!matchesQuery(query, software.getName(), software.getDescription())) {
				continue;
			}

			List<String> categoryList = extractCategoryUids(software.getCategory());
			List<AvailableFormat> formats = createFormatsForSourceCode(software);

			DiscoveryItem discoveryItem = createSoftwareDiscoveryItem(
					software.getInstanceId(),
					software.getUid(),
					software.getName(),
					software.getDescription(),
					formats,
					categoryList);

			addKeywordsFromSoftware(software.getKeywords(), keywords);
			discoveryItems.add(discoveryItem);
		}
	}

	private static void processSoftwareApplications(
			String query,
			List<SoftwareApplication> softwareApplications,
			Set<DiscoveryItem> discoveryItems,
			Set<String> keywords) {
		for (SoftwareApplication software : softwareApplications) {
			if (!matchesQuery(query, software.getName(), software.getDescription())) {
				continue;
			}

			List<String> categoryList = extractCategoryUids(software.getCategory());
			List<AvailableFormat> formats = createFormatsForApplication(software);

			DiscoveryItem discoveryItem = createSoftwareDiscoveryItem(
					software.getInstanceId(),
					software.getUid(),
					software.getName(),
					software.getDescription(),
					formats,
					categoryList);

			addKeywordsFromSoftware(software.getKeywords(), keywords);
			discoveryItems.add(discoveryItem);
		}
	}

	private static boolean hasValidCategory(DataProduct dataProduct) {
		return dataProduct.getCategory() != null && !dataProduct.getCategory().isEmpty();
	}

	private static Optional<Category> findSoftwareCategory(String instanceId, List<Category> categories) {
		return categories.stream()
				.filter(category -> category.getInstanceId().equals(instanceId))
				.filter(category -> Facets.getCategoryType(category).equals(Facets.Type.SOFTWARE))
				.findFirst();
	}

	private static void addDistributionsToDiscovery(DataProduct dataProduct, Category category,
			Set<DiscoveryItem> discoveryItems) {
		for (var distributionEntity : dataProduct.getDistribution()) {
			Distribution distribution = (Distribution) AbstractAPI.retrieveAPI(EntityNames.DISTRIBUTION.name())
					.retrieve(distributionEntity.getInstanceId());

			if (Objects.isNull(distribution)) {
				continue;
			}

			DiscoveryItem discoveryItem = new DiscoveryItemBuilder(
					distribution.getInstanceId(),
					EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getInstanceId(),
					null)
					.uid(distribution.getUid())
					.metaId(distribution.getMetaId())
					.title(distribution.getTitle() != null ? String.join(";", distribution.getTitle()) : null)
					.description(distribution.getDescription() != null
							? String.join(";", distribution.getDescription())
							: null)
					.availableFormats(AvailableFormatsGeneration.generate(distribution))
					.categories(Arrays.asList(category.getUid()))
					.build();

			discoveryItems.add(discoveryItem);
		}
	}

	private static List<String> extractCategoryUids(List<org.epos.eposdatamodel.LinkedEntity> categoryEntities) {
		return categoryEntities.stream()
				.map(linkedEntity -> (Category) LinkedEntityAPI.retrieveFromLinkedEntity(linkedEntity))
				.map(Category::getUid)
				.filter(uid -> uid.contains("category:"))
				.collect(Collectors.toList());
	}

	private static List<AvailableFormat> createFormatsForSourceCode(SoftwareSourceCode software) {
		if (software.getDownloadURL() != null) {
			return createFormatsFromUrl(software.getDownloadURL());
		} else if (software.getCodeRepository() != null) {
			return createFormatsFromUrl(software.getCodeRepository());
		}
		return null;
	}

	private static List<AvailableFormat> createFormatsForApplication(SoftwareApplication software) {
		if (software.getDownloadURL() != null) {
			return createFormatsFromUrl(software.getDownloadURL());
		} else if (software.getMainEntityOfPage() != null) {
			return createFormatsFromUrl(software.getMainEntityOfPage());
		}
		return null;
	}

	private static List<AvailableFormat> createFormatsFromUrl(String url) {
		String format = extractFormatFromUrl(url);
		if (format != null) {
			format = format.toUpperCase();
			return List.of(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat(format)
					.format(format)
					.href(url)
					.label(format)
					.type(AvailableFormatType.ORIGINAL)
					.build());
		}
		return null;
	}

	private static String extractFormatFromUrl(String url) {
		Matcher matcher = FORMAT_PATTERN.matcher(url);
		String format = null;
		// get the last match
		while (matcher.find()) {
			format = matcher.group(1);
		}
		return format;
	}

	private static DiscoveryItem createSoftwareDiscoveryItem(
			String instanceId,
			String uid,
			String name,
			String description,
			List<AvailableFormat> formats,
			List<String> categoryList) {
		return new DiscoveryItemBuilder(instanceId,
				EnvironmentVariables.API_HOST + API_PATH_DETAILS + instanceId, null)
				.uid(uid)
				.title(name)
				.description(description)
				.sha256id(DigestUtils.sha256Hex(uid))
				.availableFormats(formats)
				.categories(categoryList.isEmpty() ? null : categoryList)
				.build();
	}

	private static void addKeywordsFromSoftware(String keywordsString, Set<String> keywords) {
		keywords.addAll(Arrays.stream(
				Optional.ofNullable(keywordsString)
						.orElse("")
						.split(",\t"))
				.map(String::toLowerCase)
				.map(String::trim)
				.collect(Collectors.toList()));
	}

	private static SearchResponse buildSearchResponse(Set<DiscoveryItem> discoveryItems, Set<String> keywords) {
		Node results = new Node("results");
		var facets = FacetsGeneration.generateResponseUsingCategories(discoveryItems, Facets.Type.SOFTWARE).getFacets();
		results.addChild(facets);

		List<String> keywordsCollection = keywords.stream()
				.filter(Objects::nonNull)
				.filter(s -> !s.isEmpty())
				.sorted()
				.collect(Collectors.toList());

		NodeFilters keywordsNodes = new NodeFilters("keywords");
		keywordsCollection.forEach(keyword -> {
			NodeFilters node = new NodeFilters(keyword);
			node.setId(Base64.getEncoder().encodeToString(keyword.getBytes()));
			keywordsNodes.addChild(node);
		});

		ArrayList<NodeFilters> filters = new ArrayList<>();
		filters.add(keywordsNodes);

		return new SearchResponse(results, filters);
	}

	private static boolean matchesQuery(String query, String title, String description) {
		if (query == null || query.isEmpty())
			return true;
		String lowerQuery = query.toLowerCase();
		return title.toLowerCase().contains(lowerQuery) || description.toLowerCase().contains(lowerQuery);
	}

	private static class DataCollector {
		final List<DataProduct> dataProducts;
		final List<Category> categories;
		final List<SoftwareApplication> softwareApplications;
		final List<SoftwareSourceCode> softwareSourceCodes;

		@SuppressWarnings("unchecked")
		DataCollector() {
			this.dataProducts = ((List<DataProduct>) AbstractAPI.retrieveAPI(EntityNames.DATAPRODUCT.name())
					.retrieveAll()).stream()
					.filter(d -> d.getStatus().equals(StatusType.PUBLISHED))
					.collect(Collectors.toList());

			this.categories = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name())
					.retrieveAll();

			this.softwareApplications = (List<SoftwareApplication>) AbstractAPI
					.retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll();

			this.softwareSourceCodes = (List<SoftwareSourceCode>) AbstractAPI
					.retrieveAPI(EntityNames.SOFTWARESOURCECODE.name()).retrieveAll();
		}
	}
}
