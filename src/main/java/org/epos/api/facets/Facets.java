package org.epos.api.facets;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.CategoryScheme;
import org.epos.handler.dbapi.dbapiimplementation.CategoryDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.CategorySchemeDBAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Facets {

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private JsonObject facetsStatic;
	private JsonObject facetsFromDatabase;

	private Facets() {

		final Runnable updater = new Runnable() {
			public void run() { 
				try {
					System.out.println("Get Facets from database");
					facetsFromDatabase = generateFacetsFromDatabase();
				}catch(Exception e) {}
				System.out.println("Facets created successfully");
			}
		};
		scheduler.scheduleAtFixedRate(updater, 0, 15, TimeUnit.MINUTES);
	}

	private static Facets facets;

	public static Facets getInstance() {
		System.out.println("Get Facets instance");
		if(facets==null) facets = new Facets();
		return facets;
	}

	private JsonObject generateFacetsFromDatabase() throws IOException {
		JsonArray domainsFacets = new JsonArray();
		JsonObject facetsObject = new JsonObject();

		CategorySchemeDBAPI schemes = new CategorySchemeDBAPI();
		CategoryDBAPI categories = new CategoryDBAPI();

		List<CategoryScheme> categorySchemesList = schemes.getAll().stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());
		List<Category> categoriesList = categories.getAll().stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());

		for(CategoryScheme scheme : categorySchemesList) {
			JsonObject facetDomain = new JsonObject();
			facetDomain.addProperty("name", scheme.getTitle());
			facetDomain.add("children", recursiveChildren(categoriesList, scheme.getInstanceId(),null));
			domainsFacets.add(facetDomain);
		}

		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;

	}

	private JsonArray recursiveChildren(List<Category> categoriesList, String domain, String father) {
		JsonArray children = new JsonArray();
		if(father==null) {
			for(Category cat : categoriesList) {
				if(cat.getName().equals("Geochemical data")) System.out.println("FOUND Geochemical Data in FATHER NULL");
				if(cat.getInScheme()!=null && cat.getInScheme().equals(domain)) {
					if(cat.getBroader()==null) {
						JsonObject facetsObject = new JsonObject();
						facetsObject.addProperty("name", cat.getName());
						facetsObject.addProperty("ddss", cat.getUid());
						if(cat.getNarrower()!=null){
							JsonArray childrenList = recursiveChildren(categoriesList, domain, cat.getInstanceId());
							if(!(childrenList.size()==0))
								facetsObject.add("children", childrenList);
						}
						children.add(facetsObject);
					}
				}
			}
		} else {
			for(Category cat : categoriesList) {
				if(cat.getName().equals("Geochemical data")) System.out.println("FOUND Geochemical Data in FATHER "+father);
				if(cat.getInScheme()!=null && cat.getInScheme().equals(domain)) {
					if(cat.getBroader()!=null && cat.getBroader().contains(father)) {
						JsonObject facetsObject = new JsonObject();
						facetsObject.addProperty("name", cat.getName());
						facetsObject.addProperty("ddss", cat.getUid());
						if(cat.getNarrower() != null) {
							JsonArray childrenList = recursiveChildren(categoriesList, domain, cat.getInstanceId());
							if(!(childrenList.size()==0))
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
