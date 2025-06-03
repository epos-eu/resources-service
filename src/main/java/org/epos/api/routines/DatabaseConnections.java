package org.epos.api.routines;

import static abstractapis.AbstractAPI.retrieveAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dao.EposDataModelDAO;
import org.epos.api.beans.Plugin;
import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.Address;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.CategoryScheme;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.Equipment;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.Identifier;
import org.epos.eposdatamodel.Location;
import org.epos.eposdatamodel.Mapping;
import org.epos.eposdatamodel.Operation;
import org.epos.eposdatamodel.Organization;
import org.epos.eposdatamodel.OutputMapping;
import org.epos.eposdatamodel.Payload;
import org.epos.eposdatamodel.PeriodOfTime;
import org.epos.eposdatamodel.SoftwareApplication;
import org.epos.eposdatamodel.User;
import org.epos.eposdatamodel.WebService;
import org.epos.handler.dbapi.service.EntityManagerService;
import org.epos.router_framework.RpcRouter;
import org.epos.router_framework.RpcRouterBuilder;
import org.epos.router_framework.domain.Actor;
import org.epos.router_framework.domain.BuiltInActorType;
import org.epos.router_framework.domain.Request;
import org.epos.router_framework.domain.RequestBuilder;
import org.epos.router_framework.domain.Response;
import org.epos.router_framework.types.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import metadataapis.EntityNames;
import usermanagementapis.UserGroupManagementAPI;

public class DatabaseConnections {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnections.class);

	private List<DataProduct> dataproducts;
	private List<SoftwareApplication> softwareApplications;
	private List<Organization> organizationList;
	private List<Category> categoryList;
	private List<CategoryScheme> categorySchemesList;
	private List<Distribution> distributionList;
	private List<Operation> operationList;
	private List<WebService> webServiceList;
	private List<Address> addressList;
	private List<Location> locationList;
	private List<PeriodOfTime> periodOfTimeList;
	private List<Identifier> identifierList;
	private List<Mapping> mappingList;
	private List<Equipment> equipmentList;
	private List<Facility> facilityList;
	private List<OutputMapping> outputMappingsList;
	private List<Payload> payloadsList;
	private Map<String, User> usersMap;

	// distributionId -> list of relations with plugins
	private Map<String, List<Plugin.Relations>> plugins;
	private RpcRouter router;

	private int maxDbConnections = 18;
	private static DatabaseConnections connections;
	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static int currentErrors = 0;

	private DatabaseConnections() {
		try {
			router = RpcRouterBuilder.instance(Actor.getInstance(BuiltInActorType.CONVERTER))
					.addServiceSupport(ServiceType.METADATA, Actor.getInstance(BuiltInActorType.CONVERTER))
					.setNumberOfPublishers(1)
					.setNumberOfConsumers(1)
					.setRoutingKeyPrefix("resources")
					.build().get();
			router.init(
					System.getenv("BROKER_HOST"),
					System.getenv("BROKER_VHOST"),
					System.getenv("BROKER_USERNAME"),
					System.getenv("BROKER_PASSWORD"));
			LOGGER.info("[CONNECTION] Router initialized");
		} catch (Exception e) {
			LOGGER.error("[CONNECTION ERROR] Error while initializing router", e);
		}

		try {
			int connPoolMaxSize = Integer.parseInt(System.getenv("CONNECTION_POOL_MAX_SIZE"));
			this.maxDbConnections = Math.min(connPoolMaxSize, this.maxDbConnections);
		} catch (NumberFormatException e) {
			LOGGER.error("Error while parsing env variable CONNECTION_POOL_MAX_SIZE", e);
		}
	}

	// create a safe future handling possible null values and exceptions
	private <T> CompletableFuture<T> createSafeFuture(
			Supplier<T> supplier,
			T defaultValue,
			String operationName,
			ExecutorService executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				T result = supplier.get();
				if (result == null) {
					currentErrors++;
					return defaultValue;
				}
				return result;
			} catch (Exception e) {
				LOGGER.error("[CONNECTION ERROR] Error in {}", operationName, e);
				currentErrors++;
				return defaultValue;
			}
		}, executor);
	}

	public void syncDatabaseConnections() {

		// one thread for each query
		ExecutorService executor = Executors.newFixedThreadPool(maxDbConnections);

		EntityManagerService.getInstance().getCache().evictAll();
		// wrap each query in a future
		CompletableFuture<List<DataProduct>> tempDataproductsFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.DATAPRODUCT.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI DATAPRODUCT",
				executor);

		CompletableFuture<List<SoftwareApplication>> tempSoftwareApplicationsFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI SOFTWAREAPPLICATION",
				executor);

		CompletableFuture<List<Organization>> tempOrganizationListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.ORGANIZATION.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI ORGANIZATION",
				executor);

		CompletableFuture<List<Category>> tempCategoryListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI CATEGORY",
				executor);

		CompletableFuture<List<CategoryScheme>> tempCategorySchemeListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.CATEGORYSCHEME.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI CATEGORYSCHEME",
				executor);

		CompletableFuture<List<Distribution>> tempDistributionListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.DISTRIBUTION.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI DISTRIBUTION",
				executor);

		CompletableFuture<List<Operation>> tempOperationListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.OPERATION.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI OPERATION",
				executor);

		CompletableFuture<List<WebService>> tempWebServiceListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.WEBSERVICE.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI WEBSERVICE",
				executor);

		CompletableFuture<List<Address>> tempAddressListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.ADDRESS.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI ADDRESS",
				executor);

		CompletableFuture<List<Location>> tempLocationListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.LOCATION.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI LOCATION",
				executor);

		CompletableFuture<List<PeriodOfTime>> tempPeriodOfTimeListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.PERIODOFTIME.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI PERIODOFTIME",
				executor);

		CompletableFuture<List<Identifier>> tempIdentifierListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.IDENTIFIER.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI IDENTIFIER",
				executor);

		CompletableFuture<List<Mapping>> tempMappingListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.MAPPING.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI MAPPING",
				executor);

		CompletableFuture<List<Facility>> tempFacilityListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.FACILITY.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI FACILITY",
				executor);

		CompletableFuture<List<Equipment>> tempEquipmentListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.EQUIPMENT.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI EQUIPMENT",
				executor);

		CompletableFuture<List<OutputMapping>> tempOutputMappingListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.OUTPUTMAPPING.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI OUTPUTMAPPING",
				executor);

		CompletableFuture<List<Payload>> tempPayloadListFuture = createSafeFuture(
				() -> retrieveAPI(EntityNames.PAYLOAD.name()).retrieveAll(),
				new ArrayList<>(),
				"retrieveAPI PAYLOAD",
				executor);

		CompletableFuture<List<User>> tempUserListFuture = createSafeFuture(
				() -> UserGroupManagementAPI.retrieveAllUsers(),
				new ArrayList<>(),
				"UserGroupManagementAPI.retrieveAllUsers",
				executor);

		CompletableFuture<Map<String, List<Plugin.Relations>>> tempPluginsFuture = createSafeFuture(
				() -> retrievePlugins(),
				new HashMap<>(),
				"retrievePlugins",
				executor);

		// join the futures together
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(
				tempDataproductsFuture,
				tempSoftwareApplicationsFuture,
				tempOrganizationListFuture,
				tempCategoryListFuture,
				tempCategorySchemeListFuture,
				tempDistributionListFuture,
				tempOperationListFuture,
				tempWebServiceListFuture,
				tempAddressListFuture,
				tempLocationListFuture,
				tempPeriodOfTimeListFuture,
				tempIdentifierListFuture,
				tempMappingListFuture,
				tempFacilityListFuture,
				tempEquipmentListFuture,
				tempOutputMappingListFuture,
				tempPayloadListFuture,
				tempUserListFuture,
				tempPluginsFuture);

		// block until all done
		allFutures.join();

		// retrieve the results of the queries
		List<DataProduct> tempDataproducts = tempDataproductsFuture.join();
		List<SoftwareApplication> tempSoftwareApplications = tempSoftwareApplicationsFuture.join();
		List<Organization> tempOrganizationList = tempOrganizationListFuture.join();
		List<Category> tempCategoryList = tempCategoryListFuture.join();
		List<CategoryScheme> tempCategorySchemeList = tempCategorySchemeListFuture.join();
		List<Distribution> tempDistributionList = tempDistributionListFuture.join();
		List<Operation> tempOperationList = tempOperationListFuture.join();
		List<WebService> tempWebServiceList = tempWebServiceListFuture.join();
		List<Address> tempAddressList = tempAddressListFuture.join();
		List<Location> tempLocationList = tempLocationListFuture.join();
		List<PeriodOfTime> tempPeriodOfTimeList = tempPeriodOfTimeListFuture.join();
		List<Identifier> tempIdentifierList = tempIdentifierListFuture.join();
		List<Mapping> tempMappingList = tempMappingListFuture.join();
		List<Facility> tempFacilityList = tempFacilityListFuture.join();
		List<Equipment> tempEquipmentList = tempEquipmentListFuture.join();
		List<OutputMapping> tempOutputMappingList = tempOutputMappingListFuture.join();
		List<Payload> tempPayloadList = tempPayloadListFuture.join();
		List<User> tempUserList = tempUserListFuture.join();
		Map<String, List<Plugin.Relations>> tempPlugins = tempPluginsFuture.join();

		// convert the list to a map <id, object> for faster retrieval, with null safety
		Map<String, User> tempUsersMap = createSafeUserMap(tempUserList);

		lock.writeLock().lock();

		// hot-swap the temp variables
		try {
			dataproducts = tempDataproducts;
			softwareApplications = tempSoftwareApplications;
			organizationList = tempOrganizationList;
			categoryList = tempCategoryList;
			categorySchemesList = tempCategorySchemeList;
			distributionList = tempDistributionList;
			operationList = tempOperationList;
			webServiceList = tempWebServiceList;
			addressList = tempAddressList;
			locationList = tempLocationList;
			periodOfTimeList = tempPeriodOfTimeList;
			identifierList = tempIdentifierList;
			mappingList = tempMappingList;
			facilityList = tempFacilityList;
			equipmentList = tempEquipmentList;
			outputMappingsList = tempOutputMappingList;
			payloadsList = tempPayloadList;
			usersMap = tempUsersMap;
			plugins = tempPlugins;

			// free the executor's resources
			executor.shutdown();
		} finally {
			lock.writeLock().unlock();
		}

		if (currentErrors >= maxDbConnections) {
			LOGGER.error("Too many errors while syncing the cache ({}), exiting...", currentErrors);
			System.exit(1);
		}
	}

	private Map<String, User> createSafeUserMap(List<User> userList) {
		if (userList == null || userList.isEmpty()) {
			return new HashMap<>();
		}

		return userList.stream()
				.filter(user -> user != null && user.getAuthIdentifier() != null)
				.collect(Collectors.toMap(
						User::getAuthIdentifier,
						Function.identity(),
						(existing, replacement) -> {
							LOGGER.warn("[WARNING] Duplicate user auth identifier found, keeping existing: {}",
									existing.getAuthIdentifier());
							return existing;
						}));
	}

	public static DatabaseConnections getInstance() {
		lock.readLock().lock();
		try {
			if (connections == null) {
				connections = new DatabaseConnections();
			}
			return connections;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<DataProduct> getDataproducts() {
		return safeRead(dataproducts);
	}

	public List<SoftwareApplication> getSoftwareApplications() {
		return safeRead(softwareApplications);
	}

	public List<Organization> getOrganizationList() {
		return safeRead(organizationList);
	}

	public List<Category> getCategoryList() {
		return safeRead(categoryList);
	}

	public List<CategoryScheme> getCategorySchemesList() {
		return safeRead(categorySchemesList);
	}

	public List<Distribution> getDistributionList() {
		return safeRead(distributionList);
	}

	public List<Operation> getOperationList() {
		return safeRead(operationList);
	}

	public List<WebService> getWebServiceList() {
		return safeRead(webServiceList);
	}

	public List<Address> getAddressList() {
		return safeRead(addressList);
	}

	public List<Location> getLocationList() {
		return safeRead(locationList);
	}

	public List<PeriodOfTime> getPeriodOfTimeList() {
		return safeRead(periodOfTimeList);
	}

	public List<Identifier> getIdentifierList() {
		return safeRead(identifierList);
	}

	public List<Mapping> getMappingList() {
		return safeRead(mappingList);
	}

	public List<Facility> getFacilityList() {
		return safeRead(facilityList);
	}

	public List<Equipment> getEquipmentList() {
		return safeRead(equipmentList);
	}

	public List<OutputMapping> getOutputMappings() {
		return safeRead(outputMappingsList);
	}

	public List<Payload> getPayloads() {
		return safeRead(payloadsList);
	}

	public Map<String, User> getUsers() {
		return safeRead(usersMap);
	}

	public Map<String, List<Plugin.Relations>> getPlugins() {
		return safeRead(plugins);
	}

	private <T> T safeRead(T value) {
		lock.readLock().lock();
		try {
			return value;
		} finally {
			lock.readLock().unlock();
		}
	}

	protected Response doRequest(ServiceType service, Map<String, Object> requestParams) {
		return this.doRequest(service, null, requestParams);
	}

	protected Response doRequest(ServiceType service, Actor nextComponentOverride, Map<String, Object> requestParams) {
		Request localRequest = RequestBuilder.instance(service, "get", "plugininfo")
				.addPayloadPlainText(Utils.gson.toJson(requestParams))
				.addHeaders(new HashMap<>())
				.build();

		Response response;
		if (nextComponentOverride != null) {
			response = router.makeRequest(localRequest, nextComponentOverride);
		} else {
			response = router.makeRequest(localRequest);
		}

		return response;
	}

	private Map<String, List<Plugin.Relations>> retrievePlugins() {
		var result = new HashMap<String, List<Plugin.Relations>>();
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("plugins", "all");
			Response conversionResponse = doRequest(
					ServiceType.METADATA,
					Actor.getInstance(BuiltInActorType.CONVERTER),
					params);
			if (conversionResponse != null && conversionResponse.getPayloadAsPlainText().isPresent()) {
				var plugins = Arrays
						.stream(Utils.gson.fromJson(conversionResponse.getPayloadAsPlainText().get(), Plugin[].class))
						.collect(Collectors.toList());
				for (Plugin plugin : plugins) {
					result.putIfAbsent(plugin.getDistributionId(), plugin.getRelations());
				}
			}
		} catch (Exception e) {
			LOGGER.error("[CONNECTION ERROR] Error getting response from router", e);
		}
		return result;
	}
}
