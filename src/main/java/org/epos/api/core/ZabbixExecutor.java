package org.epos.api.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.epos.api.utility.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

public class ZabbixExecutor {

    private static ZabbixExecutor executor;
    private JsonObject hostResults;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZabbixExecutor.class);

    // API Token caricato da variabile d'ambiente
    private final String apiToken;

    public static ZabbixExecutor getInstance() {
        if(executor == null) {
            executor = new ZabbixExecutor();
        }
        return executor;
    }

    private ZabbixExecutor() {
        // Valida URL
        if (EnvironmentVariables.MONITORING_URL == null || EnvironmentVariables.MONITORING_URL.isEmpty()) {
            LOGGER.error("ZABBIX_URL non configurato!");
            LOGGER.error("Imposta la variabile d'ambiente: export ZABBIX_URL=\"http://your-server/zabbix/api_jsonrpc.php\"");
            throw new IllegalStateException("URL di Zabbix non configurato. Imposta ZABBIX_URL");
        }

        // Valida API Token
        this.apiToken = EnvironmentVariables.MONITORING_API_TOKEN;
        if (apiToken == null || apiToken.isEmpty()) {
            LOGGER.error("ZABBIX_API_TOKEN non configurato!");
            LOGGER.error("Imposta la variabile d'ambiente: export ZABBIX_API_TOKEN=\"your-api-token\"");
            throw new IllegalStateException("API Token di Zabbix non configurato. Imposta ZABBIX_API_TOKEN");
        }

        LOGGER.info("ZabbixExecutor inizializzato con API Token per Zabbix 7.4");
        LOGGER.info("Zabbix URL: {}", EnvironmentVariables.MONITORING_URL);
    }

    /**
     * Restituisce l'API Token configurato
     * Non è più necessario fare login/logout
     */
    public String getApiToken() {
        return apiToken;
    }

    /**
     * Metodo base per effettuare richieste HTTP a Zabbix 7.4
     * Utilizza l'header Authorization: Bearer per l'autenticazione
     */
    private String sendRequest(String requestBodyJson) throws IOException, InterruptedException {
        return sendRequest(requestBodyJson, true);
    }

    /**
     * Metodo base per effettuare richieste HTTP a Zabbix 7.4
     * @param requestBodyJson Il body della richiesta in JSON
     * @param useAuth Se true, include l'header Authorization Bearer
     */
    private String sendRequest(String requestBodyJson, boolean useAuth) throws IOException, InterruptedException {
        var values = Utils.gson.fromJson(requestBodyJson, HashMap.class);
        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(values);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(EnvironmentVariables.MONITORING_URL))
                .header("Content-Type", "application/json-rpc")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        // Aggiungi Authorization header solo se richiesto
        if (useAuth) {
            requestBuilder.header("Authorization", "Bearer " + apiToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        LOGGER.debug("Zabbix response: {}", response.body());
        return response.body();
    }

    /**
     * Recupera la lista degli host da Zabbix
     */
    public String retrieveHosts() throws IOException, InterruptedException {
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
                + "    \"id\": 2\n"
                + "}";

        return sendRequest(retrieveHosts);
    }

    /**
     * Recupera tutti gli item da Zabbix
     * Per filtrare per host specifico, usa getItemsForHost(hostid)
     */
    public String getItems() throws IOException, InterruptedException {
        String retrieveItems = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"item.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"itemid\",\n"
                + "            \"name\",\n"
                + "            \"key_\",\n"
                + "            \"lastclock\",\n"
                + "            \"lastvalue\",\n"
                + "            \"status\"\n"
                + "        ],\n"
                + "        \"webitems\": true,\n"
                + "        \"selectHosts\": [\n"
                + "            \"hostid\",\n"
                + "            \"host\"\n"
                + "        ]\n"
                + "    },\n"
                + "    \"id\": 2\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera gli item per un host specifico
     */
    public String getItemsForHost(String hostid) throws IOException, InterruptedException {
        String retrieveItems = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"item.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"itemid\",\n"
                + "            \"name\",\n"
                + "            \"key_\",\n"
                + "            \"lastclock\",\n"
                + "            \"lastvalue\",\n"
                + "            \"status\"\n"
                + "        ],\n"
                + "        \"hostids\": [\"" + hostid + "\"],\n"
                + "        \"selectHosts\": [\n"
                + "            \"hostid\",\n"
                + "            \"host\"\n"
                + "        ],\n"
                + "        \"sortfield\": \"name\"\n"
                + "    },\n"
                + "    \"id\": 2\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera gli item web.test.error da Zabbix
     */
    public String getWebTestErrorItems() throws IOException, InterruptedException {
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
                + "        \"selectHosts\": [\n"
                + "            \"hostid\",\n"
                + "            \"host\"\n"
                + "        ],\n"
                + "        \"search\": {\n"
                + "            \"key_\": \"web.test.error\"\n"
                + "        },\n"
                + "        \"searchByAny\": true\n"
                + "    },\n"
                + "    \"id\": 2\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera tutti i problemi da Zabbix
     * Per filtrare per host specifico, usa getProblemsForHost(hostid)
     */
    public String getProblems() throws IOException, InterruptedException {
        String retrieveItems = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"problem.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"eventid\",\n"
                + "            \"objectid\",\n"
                + "            \"name\",\n"
                + "            \"severity\",\n"
                + "            \"clock\"\n"
                + "        ],\n"
                + "        \"recent\": false,\n"
                + "        \"sortfield\": \"eventid\",\n"
                + "        \"sortorder\": \"DESC\"\n"
                + "    },\n"
                + "    \"id\": 3\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera i problemi per un host specifico
     */
    public String getProblemsForHost(String hostid) throws IOException, InterruptedException {
        String retrieveItems = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"problem.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"eventid\",\n"
                + "            \"objectid\",\n"
                + "            \"name\",\n"
                + "            \"severity\",\n"
                + "            \"clock\"\n"
                + "        ],\n"
                + "        \"hostids\": [\"" + hostid + "\"],\n"
                + "        \"recent\": false,\n"
                + "        \"sortfield\": \"clock\",\n"
                + "        \"sortorder\": \"DESC\"\n"
                + "    },\n"
                + "    \"id\": 3\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera gli HTTP test per un host specifico
     * @param hostid L'ID dell'host per cui recuperare gli HTTP test
     */
    public String getHttpItems(String hostid) throws IOException, InterruptedException {
        if (hostid == null || hostid.isEmpty()) {
            throw new IllegalArgumentException("hostid non può essere null o vuoto");
        }

        String retrieveItems = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"httptest.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"httptestid\",\n"
                + "            \"name\",\n"
                + "            \"status\"\n"
                + "        ],\n"
                + "        \"hostids\": [\"" + hostid + "\"],\n"
                + "        \"selectHosts\": [\n"
                + "            \"hostid\",\n"
                + "            \"host\"\n"
                + "        ],\n"
                + "        \"selectSteps\": \"extend\"\n"
                + "    },\n"
                + "    \"id\": 1\n"
                + "}";

        return sendRequest(retrieveItems);
    }

    /**
     * Recupera gli HTTP test (alias per getGraphs)
     */
    public String getHttpTest() throws IOException, InterruptedException {
        return getGraphs();
    }

    /**
     * Recupera tutti gli HTTP test da Zabbix
     * Per filtrare per host specifico, usa getHttpItems(hostid)
     */
    public String getGraphs() throws IOException, InterruptedException {
        String httpTest = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"httptest.get\",\n"
                + "    \"params\": {\n"
                + "        \"output\": [\n"
                + "            \"httptestid\",\n"
                + "            \"name\",\n"
                + "            \"status\"\n"
                + "        ],\n"
                + "        \"selectHosts\": [\n"
                + "            \"hostid\",\n"
                + "            \"host\"\n"
                + "        ],\n"
                + "        \"selectSteps\": \"extend\"\n"
                + "    },\n"
                + "    \"id\": 4\n"
                + "}";

        return getResultAsString(httpTest);
    }

    /**
     * Effettua una richiesta e restituisce il campo "result" come stringa
     */
    private String getResultAsString(String requestJson) throws IOException, InterruptedException {
        return getResultAsString(requestJson, true);
    }

    /**
     * Effettua una richiesta e restituisce il campo "result" come stringa
     * @param requestJson Il JSON della richiesta
     * @param useAuth Se true, include l'header Authorization Bearer
     */
    private String getResultAsString(String requestJson, boolean useAuth) throws IOException, InterruptedException {
        var values = Utils.gson.fromJson(requestJson, HashMap.class);
        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(values);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(EnvironmentVariables.MONITORING_URL))
                .header("Content-Type", "application/json-rpc")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        // Aggiungi Authorization header solo se richiesto
        if (useAuth) {
            requestBuilder.header("Authorization", "Bearer " + apiToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = Utils.gson.fromJson(response.body(), JsonObject.class);

        // Gestisce eventuali errori dall'API
        if (jsonResponse.has("error")) {
            JsonObject error = jsonResponse.getAsJsonObject("error");
            String errorMessage = error.get("message").getAsString();
            String errorData = error.has("data") ? error.get("data").getAsString() : "";
            LOGGER.error("Errore Zabbix API: {} - {}", errorMessage, errorData);
            throw new IOException("Zabbix API Error: " + errorMessage + " - " + errorData);
        }

        return jsonResponse.get("result").getAsString();
    }

    // Getter e Setter per hostResults
    public JsonObject getHostResults() {
        return hostResults;
    }

    public void setHostResults(JsonObject hostResults) {
        this.hostResults = hostResults;
    }

    /**
     * Recupera lo status da un ID SHA
     */
    public Integer getStatusInfoFromSha(String idSha) {
        if(hostResults == null) return 0;
        if(!hostResults.has(idSha)) return 0;
        if(!hostResults.get(idSha).getAsJsonObject().has("status")) return 0;

        return hostResults.get(idSha).getAsJsonObject().get("status").getAsInt();
    }

    /**
     * Recupera il timestamp dello status da un ID SHA
     */
    public String getStatusTimestampInfoFromSha(String idSha) {
        if(hostResults == null) return null;
        if(!hostResults.has(idSha)) return null;
        if(!hostResults.get(idSha).getAsJsonObject().has("timestamp")) return null;

        return hostResults.get(idSha).getAsJsonObject().get("timestamp").getAsString();
    }

    /**
     * Verifica la connessione con Zabbix controllando la versione API
     * Nota: apiinfo.version NON richiede autenticazione in Zabbix 7.4
     */
    public String getApiVersion() throws IOException, InterruptedException {
        String versionRequest = "{\n"
                + "    \"jsonrpc\": \"2.0\",\n"
                + "    \"method\": \"apiinfo.version\",\n"
                + "    \"params\": {},\n"
                + "    \"id\": 1\n"
                + "}";

        // apiinfo.version deve essere chiamato SENZA header Authorization
        String response = sendRequest(versionRequest, false);
        JsonObject jsonResponse = Utils.gson.fromJson(response, JsonObject.class);

        if (jsonResponse.has("result")) {
            return jsonResponse.get("result").getAsString();
        }

        if (jsonResponse.has("error")) {
            JsonObject error = jsonResponse.getAsJsonObject("error");
            String errorMessage = error.get("message").getAsString();
            LOGGER.error("Errore recupero versione API: {}", errorMessage);
        }

        return null;
    }
}