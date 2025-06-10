package org.epos.api.routines;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jakarta.annotation.PostConstruct;

import dao.EposDataModelDAO;
import org.apache.commons.lang3.StringUtils;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.facets.Facets;
import org.epos.api.utility.Utils;
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
		LOGGER.info("[StartUp Task - Resources] Updating facets information");
		connectionsUpdater();
		LOGGER.info("[StartUp Task - Resources] Done");
		LOGGER.info("[StartUp Task - Monitoring] Updating monitoring information");
		zabbixUpdater();
		LOGGER.info("[StartUp Task - Monitoring] Done");
		LOGGER.info("[StartUp Task - Facets] Updating facets information");
		facetsUpdater();
		LOGGER.info("[StartUp Task - Facets] Done");
		LOGGER.info("[Resources Service Startup Completed] -----------------------------------------------");
	}

	@Scheduled(fixedRate = 90000, initialDelay = 0)
	@Async
	public void zabbixUpdater() {
		if(EnvironmentVariables.MONITORING != null && EnvironmentVariables.MONITORING.equals("true")) {
			LOGGER.info("[Scheduled Task - Monitoring] Updating monitoring information");
			JsonObject hostResults = new JsonObject();
			String auth;
			try {
				auth = ZabbixExecutor.auth();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				//String items = ZabbixExecutor.getItems(auth);
				ArrayList<String> listOfIds = new ArrayList<String>();
				Utils.gson.fromJson(ZabbixExecutor.getProblems(auth), JsonObject.class).get("result").getAsJsonArray()
				.forEach(e->listOfIds.add(getSubString(e.getAsJsonObject().get("name").getAsString())));

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
	}

	@Scheduled(fixedRate = 90000, initialDelay = 0)
	@Async
	public void facetsUpdater() {
		LOGGER.info("[Scheduled Task - Facets] Updating facets information");

		try {
			var dataFacets = Facets.getInstance().generateFacetsFromDatabase(Facets.Type.DATA);
			var facilityFacets = Facets.getInstance().generateFacetsFromDatabase(Facets.Type.FACILITY);
			Facets.getInstance().setFacetsFromDatabase(dataFacets, Facets.Type.DATA);
			Facets.getInstance().setFacetsFromDatabase(facilityFacets, Facets.Type.FACILITY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("[Scheduled Task - Facets] Facets successfully updated");
	}

	@Scheduled(fixedRate = 90000, initialDelay = 0)
	@Async
	public void connectionsUpdater() {
		LOGGER.info("[Scheduled Task - Resources] Updating resources information");
		EposDataModelDAO.clearAllCaches();
        DatabaseConnections.getInstance().syncDatabaseConnections();
        LOGGER.info("[Scheduled Task - Resources] Resources successfully updated");
	}

	private String getSubString(final String input) {
		if(input == null) {
			return null;
		}

		final int indexOfAt = input.indexOf('"');
		if(input.isEmpty() || indexOfAt < 0 || indexOfAt > input.length()-1) {
			return null;
		}

		String suffix = input.substring(indexOfAt + 1);

		final int indexOfDot = suffix.indexOf('"');

		if(indexOfDot < 1) {
			return null;
		}

		return suffix.substring(0, indexOfDot);
	}

}
