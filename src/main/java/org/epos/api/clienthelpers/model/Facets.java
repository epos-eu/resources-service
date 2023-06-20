package org.epos.api.clienthelpers.model;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Facets {

	private static File[] getResourceFolderFiles(String folder) {
		return new File(folder).listFiles();
	}

	private Facets() {}

	public static JsonObject getFacets() throws IOException {
		Gson gson = new Gson();
		JsonObject facetsObject = new JsonObject();
		JsonArray domainsFacets = new JsonArray();
		JsonObject json;

		for (File f : getResourceFolderFiles("facets")) {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			json = gson.fromJson(bufferedReader, JsonArray.class).get(0).getAsJsonObject();
			domainsFacets.add(json);
		}


		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;
	}
}
