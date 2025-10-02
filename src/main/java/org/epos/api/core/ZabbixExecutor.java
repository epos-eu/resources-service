package org.epos.api.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.epos.api.utility.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

public class ZabbixExecutor {

	private static ZabbixExecutor executor;

	private JsonObject hostResults;

	private static final Logger LOGGER = LoggerFactory.getLogger(ZabbixExecutor.class); 

	public static ZabbixExecutor getInstance() {
		if(executor==null) executor = new ZabbixExecutor();
		return executor;
	}

	private ZabbixExecutor() {}


	public static String auth() throws IOException, InterruptedException {

		String authRequest = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"user.login\",\n"
				+ "    \"params\": {\n"
				+ "        \"username\": \""+EnvironmentVariables.MONITORING_USER+"\",\n"
				+ "        \"password\": \""+EnvironmentVariables.MONITORING_PWD+"\"\n"
				+ "    },\n"
				+ "    \"id\": 1\n"
				+ "}";
		return getStringFromJson(authRequest);
	}

	public static void logout(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\"jsonrpc\":\"2.0\",\"method\":\"user.logout\",\"params\":[],\"auth\":\""+auth+"\",\"id\":6}\n";

		getString(retrieveItems);
	}

	private static String getString(String retrieveItems) throws IOException, InterruptedException {
		var values = Utils.gson.fromJson(retrieveItems, HashMap.class);
		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);


		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}


	public static String retrieveHosts(String auth) throws IOException, InterruptedException {


		String retrieveHosts = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"host.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"selectInterfaces\": [\n"
				+ "            \"interfaceid\",\n"
				+ "            \"ip\"\n"
				+ "        ]\n"
				+ "    },\n"
				+ "    \"id\": 2,\n"
				+ "    \"auth\": \""+ auth+"\"\n"
				+ "}";

		return getString(retrieveHosts);
	}

	public static String getItems(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"item.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"itemid\",\n"
				+ "            \"name\",\n"
				+ "            \"key_\",\n"
				+ "            \"lastclock\",\n"
				+ "            \"lastvalue\"\n"
				+ "        ],\n"
				+ "        \"webitems\": true,\n"
				+ "        \"history\": \"0\",\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"selectHosts\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"search\": {\n"
				+ "            \"key_\": \"web.test.error\"\n"
				+ "        },\n"
				+ "        \"selectApplications\": [\n"
				+ "            \"applicationid\"\n"
				+ "        ]\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 2\n"
				+ "}";

		return getString(retrieveItems);
	}

	public static String getProblems(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"problem.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"name\",\n"
				+ "            \"clock\"\n"
				+ "        ],\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"recent\": false\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 3\n"
				+ "}";

		return getString(retrieveItems);
	}

	public static String getHttpItems(String auth, String hostid) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "        \"jsonrpc\": \"2.0\",\n"
				+ "        \"method\": \"httptest.get\",\n"
				+ "        \"params\": {\n"
				+ "          \"output\": [\"name\", \"httptestid\"],\n"
				+ "          \"recent\" : false,\n"
				+ "          \"filter\": {\n"
				+ "              \"hostid\" : \""+hostid+"\"\n"
				+ "          },\n"
				+ "          \"selectHosts\" : [\"hostid\", \"host\"]\n"
				+ "        },\n"
				+ "        \"auth\": \""+auth+"\",\n"
				+ "        \"id\": 1\n"
				+ "      }";

		return getString(retrieveItems);
	}

	protected static String getHttpTest(String auth) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
        return getGraphs(auth);
	}

	protected static String getGraphs(String auth) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String httpTest = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"httptest.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"name\",\n"
				+ "            \"httptestid\"\n"
				+ "        ],\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"selectHosts\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"recent\": false\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 4\n"
				+ "}";
		return getStringFromJson(httpTest);
	}

	private static String getStringFromJson(String httpTest) throws IOException, InterruptedException {
		var values = Utils.gson.fromJson(httpTest, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());


		return Utils.gson.fromJson(response.body(), JsonObject.class).get("result").getAsString();
	}


	public JsonObject getHostResults() {
		return hostResults;
	}

	public void setHostResults(JsonObject hostResults) {
		this.hostResults = hostResults;
	}
	
	public Integer getStatusInfoFromSha(String idSha) {
		if(!hostResults.has(idSha)) return 0;
		if(!hostResults.get(idSha).getAsJsonObject().has("status")) return 0;
		
		return hostResults.get(idSha).getAsJsonObject().get("status").getAsInt();
	}
	
	public String getStatusTimestampInfoFromSha(String idSha) {
		if(!hostResults.has(idSha)) return null;
		if(!hostResults.get(idSha).getAsJsonObject().has("timestamp")) return null;
		
		return hostResults.get(idSha).getAsJsonObject().get("timestamp").getAsString();
	}

}
