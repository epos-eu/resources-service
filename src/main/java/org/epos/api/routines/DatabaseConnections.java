package org.epos.api.routines;

import static abstractapis.AbstractAPI.retrieveAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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

import metadataapis.EntityNames;

public class DatabaseConnections {
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

	// distributionId -> list of relations with plugins
	private Map<String, List<Plugin.Relations>> plugins;
	private RpcRouter router;

	private int maxDbConnections = 17;
	private static DatabaseConnections connections;
	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
			System.out.println("[CONNECTION] Router initialized");
		} catch (Exception e) {
			System.err.println("[CONNECTION ERROR] Error while initializing router. Stack:\n" + e.toString());
		}

		try {
			int connPoolMaxSize = Integer.parseInt(System.getenv("CONNECTION_POOL_MAX_SIZE"));
			this.maxDbConnections = Math.min(connPoolMaxSize, this.maxDbConnections);
		} catch (NumberFormatException e) {
			System.err.println("Error while parsing env variable CONNECTION_POOL_MAX_SIZE: " + e.toString());
		}
	}

	public void syncDatabaseConnections() {
		if (EntityManagerService.getInstance() != null)
			EntityManagerService.getInstance().getCache().evictAll();

		// one thread for each query
		ExecutorService executor = Executors.newFixedThreadPool(maxDbConnections);

		// wrap each query in a future
		CompletableFuture<List<DataProduct>> tempDataproductsFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.DATAPRODUCT.name()).retrieveAll(), executor);

		CompletableFuture<List<SoftwareApplication>> tempSoftwareApplicationsFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll(), executor);

		CompletableFuture<List<Organization>> tempOrganizationListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.ORGANIZATION.name()).retrieveAll(), executor);

		CompletableFuture<List<Category>> tempCategoryListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll(), executor);

		CompletableFuture<List<CategoryScheme>> tempCategorySchemeListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.CATEGORYSCHEME.name()).retrieveAll(), executor);

		CompletableFuture<List<Distribution>> tempDistributionListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.DISTRIBUTION.name()).retrieveAll(), executor);

		CompletableFuture<List<Operation>> tempOperationListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.OPERATION.name()).retrieveAll(), executor);

		CompletableFuture<List<WebService>> tempWebServiceListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.WEBSERVICE.name()).retrieveAll(), executor);

		CompletableFuture<List<Address>> tempAddressListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.ADDRESS.name()).retrieveAll(), executor);

		CompletableFuture<List<Location>> tempLocationListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.LOCATION.name()).retrieveAll(), executor);

		CompletableFuture<List<PeriodOfTime>> tempPeriodOfTimeListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.PERIODOFTIME.name()).retrieveAll(), executor);

		CompletableFuture<List<Identifier>> tempIdentifierListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.IDENTIFIER.name()).retrieveAll(), executor);

		CompletableFuture<List<Mapping>> tempMappingListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.MAPPING.name()).retrieveAll(), executor);

		CompletableFuture<List<Facility>> tempFacilityListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.FACILITY.name()).retrieveAll(), executor);

		CompletableFuture<List<Equipment>> tempEquipmentListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.EQUIPMENT.name()).retrieveAll(), executor);

		CompletableFuture<List<OutputMapping>> tempOutputMappingListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.OUTPUTMAPPING.name()).retrieveAll(), executor);

		CompletableFuture<List<Payload>> tempPayloadListFuture = CompletableFuture
				.supplyAsync(() -> retrieveAPI(EntityNames.PAYLOAD.name()).retrieveAll(), executor);

		CompletableFuture<Map<String, List<Plugin.Relations>>> tempPluginsFuture = CompletableFuture.supplyAsync(
				() -> retreivePlugins(),
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
				tempPluginsFuture);

		// block until all done
		allFutures.join();

		// retreive the results of the queries
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
		Map<String, List<Plugin.Relations>> tempPlugins = tempPluginsFuture.join();

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
			plugins = tempPlugins;

			// free the executor's resources
			executor.shutdown();
		} finally {
			lock.writeLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			return dataproducts;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<SoftwareApplication> getSoftwareApplications() {
		lock.readLock().lock();
		try {
			return softwareApplications;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Organization> getOrganizationList() {
		lock.readLock().lock();
		try {
			return organizationList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Category> getCategoryList() {
		lock.readLock().lock();
		try {
			return categoryList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<CategoryScheme> getCategorySchemesList() {
		lock.readLock().lock();
		try {
			return categorySchemesList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Distribution> getDistributionList() {
		lock.readLock().lock();
		try {
			return distributionList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Operation> getOperationList() {
		lock.readLock().lock();
		try {
			return operationList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<WebService> getWebServiceList() {
		lock.readLock().lock();
		try {
			return webServiceList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Address> getAddressList() {
		lock.readLock().lock();
		try {
			return addressList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Location> getLocationList() {
		lock.readLock().lock();
		try {
			return locationList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<PeriodOfTime> getPeriodOfTimeList() {
		lock.readLock().lock();
		try {
			return periodOfTimeList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Identifier> getIdentifierList() {
		lock.readLock().lock();
		try {
			return identifierList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Mapping> getMappingList() {
		lock.readLock().lock();
		try {
			return mappingList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Facility> getFacilityList() {
		lock.readLock().lock();
		try {
			return facilityList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Equipment> getEquipmentList() {
		lock.readLock().lock();
		try {
			return equipmentList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<OutputMapping> getOutputMappings() {
		lock.readLock().lock();
		try {
			return outputMappingsList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<Payload> getPayloads() {
		lock.readLock().lock();
		try {
			return payloadsList;
		} finally {
			lock.readLock().unlock();
		}
	}

	public Map<String, List<Plugin.Relations>> getPlugins() {
		lock.readLock().lock();
		try {
			return plugins;
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

	private Map<String, List<Plugin.Relations>> retreivePlugins() {
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
			System.err.println("[CONNECTION ERROR] Error getting respoonse from router: " + e.toString());
		}
		return result;
	}
}
