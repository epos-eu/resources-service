package org.epos.api.facets;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.CategoryScheme;
import org.epos.eposdatamodel.LinkedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;

public class Facets {

	private static final Logger LOGGER = LoggerFactory.getLogger(Facets.class);

	private JsonObject facetsStatic;
	private JsonObject facetsFromDatabaseData;
	private JsonObject facetsFromDatabaseSoftware;
	private JsonObject facetsFromDatabaseFacilities;

	private Facets() {
	}

	private static Facets facets;

	public static Facets getInstance() {
		if (facets == null)
			facets = new Facets();
		return facets;
	}

	public JsonObject generateFacetsFromDatabase(Type type) throws IOException {
		JsonArray domainsFacets = new JsonArray();
		JsonObject facetsObject = new JsonObject();

		List<CategoryScheme> schemes = (List<CategoryScheme>) AbstractAPI.retrieveAPI(EntityNames.CATEGORYSCHEME.name())
				.retrieveAll();
		List<Category> categories = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll();

		List<CategoryScheme> categorySchemesList = schemes.stream()
				.filter(e -> e.getUid().contains("category:"))
				.filter(e -> getCategorySchemeType(e).equals(type))
				.collect(Collectors.toList());
		List<Category> categoriesList = categories.stream()
				.filter(e -> Objects.nonNull(e.getUid()) && e.getUid().contains("category:"))
				.collect(Collectors.toList());

		for (CategoryScheme scheme : categorySchemesList) {
			JsonObject facetDomain = new JsonObject();
			facetDomain.addProperty("name", scheme.getTitle());
			facetDomain.addProperty("code", scheme.getCode());
			facetDomain.addProperty("linkUrl", scheme.getHomepage());
			facetDomain.addProperty("id", scheme.getOrderitemnumber());
			facetDomain.addProperty("imgUrl", scheme.getLogo());
			facetDomain.addProperty("color", scheme.getColor());
			facetDomain.add("children", recursiveChildren(categoriesList, scheme.getInstanceId(), null));
			domainsFacets.add(facetDomain);
		}

		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;

	}

	private JsonArray recursiveChildren(List<Category> categoriesList, String domain, String father) {
		List<Category> tempCategoryList = categoriesList.stream()
				.filter(category -> category.getInScheme() != null)
				.filter(category -> category.getInScheme().getInstanceId().equals(domain))
				.collect(Collectors.toList());
		JsonArray children = new JsonArray();

		// If father is null, then is a top-level category
		if (father == null) {
			// Loop over the categories in the domain (inScheme) and take the categories
			// with empty broaders
			for (Category cat : tempCategoryList.stream()
					.filter(category -> category.getBroader() != null && category.getBroader().isEmpty())
					.collect(Collectors.toList())) {
				JsonObject facetsObject = new JsonObject();
				facetsObject.addProperty("name", cat.getName());
				facetsObject.addProperty("ddss", cat.getUid());
				// check if there are sons, in that case go ahead
				if (!cat.getNarrower().isEmpty()) {
					JsonArray childrenList = recursiveChildren(tempCategoryList, domain, cat.getInstanceId());
					if (!(childrenList.isEmpty()))
						facetsObject.add("children", childrenList);
				}
				children.add(facetsObject);
			}
		} else {
			// Loop over the categories in the domain (inScheme) and take the categories
			// with a populated broaders
			for (Category cat : tempCategoryList.stream().filter(category -> !category.getBroader().isEmpty())
					.collect(Collectors.toList())) {
				// check if the broader is equal to the father
				for (LinkedEntity linkedEntity : cat.getBroader()) {
					if (linkedEntity.getInstanceId().equals(father)) {
						JsonObject facetsObject = new JsonObject();
						facetsObject.addProperty("name", cat.getName());
						facetsObject.addProperty("ddss", cat.getUid());
						// check if there are sons, in that case go ahead
						if (!cat.getNarrower().isEmpty()) {
							JsonArray childrenList = recursiveChildren(tempCategoryList, domain, cat.getInstanceId());
							if (!(childrenList.isEmpty()))
								facetsObject.add("children", childrenList);
						}
						children.add(facetsObject);
					}
				}
			}
		}
		return children;
	}

	public JsonObject getFacetsStatic() {
		return facetsStatic;
	}

	public void setFacetsStatic(JsonObject facetsStatic) {
		this.facetsStatic = facetsStatic;
	}

	public JsonObject getFacetsFromDatabase(Type type) {
		switch (type) {
			case FACILITY:
				return facetsFromDatabaseFacilities;
			case DATA:
				return facetsFromDatabaseData;
			case SOFTWARE:
				return facetsFromDatabaseSoftware;
			default:
				return facetsFromDatabaseData;
		}
	}

	public void setFacetsFromDatabase(JsonObject facetsFromDatabase, Type type) {
		switch (type) {
			case FACILITY:
				this.facetsFromDatabaseFacilities = facetsFromDatabase;
				break;
			case DATA:
				this.facetsFromDatabaseData = facetsFromDatabase;
				break;
			case SOFTWARE:
				this.facetsFromDatabaseSoftware = facetsFromDatabase;
				break;
			default:
				this.facetsFromDatabaseData = facetsFromDatabase;
				break;
		}
	}

	private static Type getCategorySchemeType(CategoryScheme scheme) {
		var topConcepts = scheme.getTopConcepts();
		if (topConcepts == null || topConcepts.isEmpty()) {
			LOGGER.warn("scheme has no topConcepts, defaulting to DATA. Scheme UID: " + scheme.getUid());
			return Type.DATA;
		}

		for (LinkedEntity topConceptEntity : topConcepts) {
			Category topConcept = (Category) LinkedEntityAPI.retrieveFromLinkedEntity(topConceptEntity);
			if (topConcept == null) {
				continue;
			}

			String uid = topConcept.getUid();
			if ("category:facets/facility-theme".equals(uid)) {
				return Type.FACILITY;
			} else if ("category:facets/software-theme".equals(uid)) {
				return Type.SOFTWARE;
			} else if ("category:facets/dataset-theme".equals(uid)) {
				return Type.DATA;
			}

			LOGGER.warn("unknown category UID '" + uid + "', defaulting to DATA: " + scheme);
			return Type.DATA;
		}

		LOGGER.warn("no valid top concept found for this scheme, defaulting to DATA: " + scheme);
		return Type.DATA;
	}

	public static Type getCategoryType(Category category) {
		if (category.getInScheme() == null) {
			LOGGER.warn("category has no scheme, defaulting to DATA for type. Category: {}", category.toString());
			return Type.DATA;
		}
		var scheme = (CategoryScheme) LinkedEntityAPI.retrieveFromLinkedEntity(category.getInScheme());
		return getCategorySchemeType(scheme);
	}

	public enum Type {
		FACILITY,
		DATA,
		SOFTWARE,
	}
}
