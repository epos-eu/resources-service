package org.epos.api.facets;


import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import abstractapis.AbstractAPI;
import metadataapis.EntityNames;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.CategoryScheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.LinkedEntity;

public class Facets {
	private JsonObject facetsStatic;
	private JsonObject facetsFromDatabase;

	private Facets() {}

	private static Facets facets;

	public static Facets getInstance() {
		if(facets==null) facets = new Facets();
		return facets;
	}

	public JsonObject generateFacetsFromDatabase() throws IOException {
		JsonArray domainsFacets = new JsonArray();
		JsonObject facetsObject = new JsonObject();

		List<CategoryScheme> schemes = (List<CategoryScheme>) AbstractAPI.retrieveAPI(EntityNames.CATEGORYSCHEME.name()).retrieveAll();
		List<Category> categories = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll();

		List<CategoryScheme> categorySchemesList = schemes.stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());
		List<Category> categoriesList = categories.stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());

		for(CategoryScheme scheme : categorySchemesList) {
			JsonObject facetDomain = new JsonObject();
			facetDomain.addProperty("name", scheme.getTitle());
			facetDomain.addProperty("code", scheme.getCode());
			facetDomain.addProperty("linkUrl", scheme.getHomepage());
			facetDomain.addProperty("id", scheme.getOrderitemnumber());
			facetDomain.addProperty("imgUrl", scheme.getLogo());
			facetDomain.addProperty("color", scheme.getColor());
			facetDomain.add("children", recursiveChildren(categoriesList, scheme.getInstanceId(),null));
			domainsFacets.add(facetDomain);
		}

		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;

	}
	private JsonArray recursiveChildren(List<Category> categoriesList, String domain, String father) {
		List<Category> tempCategoryList = categoriesList.stream().filter(category -> category.getInScheme()!=null).filter(category -> category.getInScheme().getInstanceId().equals(domain)).collect(Collectors.toList());
		JsonArray children = new JsonArray();

		//If father is null, then is a top-level category
		if(father==null) {
			// Loop over the categories in the domain (inScheme) and take the categories with empty broaders
			for(Category cat : tempCategoryList.stream().filter(category -> category.getBroader().isEmpty()).collect(Collectors.toList())) {
				JsonObject facetsObject = new JsonObject();
				facetsObject.addProperty("name", cat.getName());
				facetsObject.addProperty("ddss", cat.getUid());
				// check if there are sons, in that case go ahead
				if(!cat.getNarrower().isEmpty()){
					JsonArray childrenList = recursiveChildren(tempCategoryList, domain, cat.getInstanceId());
					if(!(childrenList.isEmpty()))
						facetsObject.add("children", childrenList);
				}
				children.add(facetsObject);
			}
		} else {
			// Loop over the categories in the domain (inScheme) and take the categories with a populated broaders
			for(Category cat : tempCategoryList.stream().filter(category -> !category.getBroader().isEmpty()).collect(Collectors.toList())) {
				//check if the broader is equal to the father
				for(LinkedEntity linkedEntity : cat.getBroader()){
					if(linkedEntity.getInstanceId().equals(father)) {
						JsonObject facetsObject = new JsonObject();
						facetsObject.addProperty("name", cat.getName());
						facetsObject.addProperty("ddss", cat.getUid());
						// check if there are sons, in that case go ahead
						if(!cat.getNarrower().isEmpty()) {
							JsonArray childrenList = recursiveChildren(tempCategoryList, domain, cat.getInstanceId());
							if(!(childrenList.isEmpty()))
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

	public JsonObject getFacetsFromDatabase() {
		return facetsFromDatabase;
	}

	public void setFacetsFromDatabase(JsonObject facetsFromDatabase) {
		this.facetsFromDatabase = facetsFromDatabase;
	}

}
