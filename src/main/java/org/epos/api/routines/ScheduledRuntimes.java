package org.epos.api.routines;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.epos.api.core.SearchGenerationJPA;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.facets.Facets;
import org.epos.api.utility.Utils;
import org.epos.handler.dbapi.model.EDMDataproduct;
import org.epos.handler.dbapi.service.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class ScheduledRuntimes {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledRuntimes.class);
	
	@PostConstruct
    public void onStartup() {
		LOGGER.info("[Resources Service Startup] -----------------------------------------------");
		LOGGER.info("[StartUp Task - Monitoring] Updating monitoring information");
		zabbixUpdater();
		LOGGER.info("[StartUp Task - Monitoring] Done");
		LOGGER.info("[StartUp Task - Facets] Updating facets information");
		facetsUpdater();
		LOGGER.info("[StartUp Task - Monitoring] Done");
		LOGGER.info("[StartUp Task - Search Check] Executing search query check");
		dataproductSearchUpdater();
		LOGGER.info("[StartUp Task - Search Check] Done");
		LOGGER.info("[Resources Service Startup Completed] -----------------------------------------------");
    }

	@Scheduled(fixedRate = 60000, initialDelay = 0)
	@Async
	public void zabbixUpdater() {
		LOGGER.info("[Scheduled Task - Monitoring] Updating monitoring information");
		JsonObject hostResults = new JsonObject();
		String auth;
		try {
			auth = ZabbixExecutor.auth();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			//String items = ZabbixExecutor.getItems(auth);
			ArrayList<String> listOfIds = new ArrayList<String>();
			Utils.gson.fromJson(ZabbixExecutor.getProblems(auth), JsonObject.class).get("result").getAsJsonArray()
			.forEach(e->listOfIds.add(getSubString(e.getAsJsonObject().get("name").getAsString(),'"','"')));

			for(JsonElement item : Utils.gson.fromJson(ZabbixExecutor.getItems(auth), JsonObject.class).get("result").getAsJsonArray()) {
				if(!item.getAsJsonObject().get("name").getAsString().contains("EPOS ICS-C")) {
					JsonObject singleResult = new JsonObject();

					String id =  StringUtils.substringBetween(item.getAsJsonObject().get("key_").getAsString(), "[", "]");
					singleResult.addProperty("name", item.getAsJsonObject().get("name").getAsString());
					singleResult.addProperty("itemid", item.getAsJsonObject().get("itemid").getAsString());
					singleResult.addProperty("key_", item.getAsJsonObject().get("key_").getAsString());
					singleResult.addProperty("lastclock",item.getAsJsonObject().get("lastclock").getAsString());
					singleResult.addProperty("lastvalue",item.getAsJsonObject().get("lastvalue").getAsString());
					singleResult.addProperty("id", id);
					singleResult.addProperty("status", (!listOfIds.contains(id))? 1 : 2);
					singleResult.addProperty("timestamp",df.format(new Date(item.getAsJsonObject().get("lastclock").getAsLong()*1000)).replace(" ", "T")+"Z");

					hostResults.add(id,singleResult);
				}
			}
			ZabbixExecutor.logout(auth);

			ZabbixExecutor.getInstance().setHostResults(hostResults);

		} catch (IOException | InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		LOGGER.info("[Scheduled Task - Monitoring] Monitoring information successfully updated");
	}

	@Scheduled(fixedRate = 60000, initialDelay = 0)
	@Async
	public void facetsUpdater() {
		LOGGER.info("[Scheduled Task - Facets] Updating facets information");

		JsonObject facetsFromDatabase;
		try {
			facetsFromDatabase = Facets.getInstance().generateFacetsFromDatabase();
			Facets.getInstance().setFacetsFromDatabase(facetsFromDatabase);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getLocalizedMessage());
		}
		LOGGER.info("[Scheduled Task - Facets] Facets successfully updated");
	}
	
	@Scheduled(fixedRate = 60000, initialDelay = 0)
	@Async
	public void dataproductSearchUpdater() {
		LOGGER.info("[Scheduled Task - DataProducts Search] Updating dataProducts Search information");

		EntityManager em = new DBService().getEntityManager();

		SearchGenerationJPA.dataproducts = getFromDB(em, EDMDataproduct.class, "dataproduct.findAllByState", "STATE", "PUBLISHED");
		em.close();
		
		LOGGER.info("[Scheduled Task - Facets] Facets successfully updated");
	}

	private String getSubString(final String input, char characterStart, char characterEnd) {
		if(input == null) {
			return null;
		}

		final int indexOfAt = input.indexOf(characterStart);
		if(input.isEmpty() || indexOfAt < 0 || indexOfAt > input.length()-1) {
			return null;
		}

		String suffix = input.substring(indexOfAt + 1);

		final int indexOfDot = suffix.indexOf(characterEnd);

		if(indexOfDot < 1) {
			return null;
		}

		return suffix.substring(0, indexOfDot);
	}

}
