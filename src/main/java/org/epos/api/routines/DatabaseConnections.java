package org.epos.api.routines;

import com.google.gson.JsonObject;
import metadataapis.*;
import org.epos.api.beans.Plugin;
import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.*;
import org.epos.handler.dbapi.service.EntityManagerService;
import org.epos.router_framework.RpcRouter;
import org.epos.router_framework.RpcRouterBuilder;
import org.epos.router_framework.domain.*;
import org.epos.router_framework.types.ServiceType;

import java.util.*;
import java.util.stream.Collectors;

import static abstractapis.AbstractAPI.*;

public class DatabaseConnections {

	private RpcRouter router;

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

	private List<Plugin> plugins;

	private DatabaseConnections() {}

	public void syncDatabaseConnections() {
		if(EntityManagerService.getInstance()!=null) EntityManagerService.getInstance().getCache().evictAll();

		List<DataProduct> tempDataproducts  = retrieveAPI(EntityNames.DATAPRODUCT.name()).retrieveAll();
		List<SoftwareApplication> tempSoftwareApplications = retrieveAPI(EntityNames.SOFTWAREAPPLICATION.name()).retrieveAll();
		List<Organization> tempOrganizationList = retrieveAPI(EntityNames.ORGANIZATION.name()).retrieveAll();
		List<Category> tempCategoryList = retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll();
		List<CategoryScheme> tempCategorySchemeList = retrieveAPI(EntityNames.CATEGORYSCHEME.name()).retrieveAll();
		List<Distribution> tempDistributionList = retrieveAPI(EntityNames.DISTRIBUTION.name()).retrieveAll();
		List<Operation> tempOperationList = retrieveAPI(EntityNames.OPERATION.name()).retrieveAll();
		List<WebService> tempWebServiceList = retrieveAPI(EntityNames.WEBSERVICE.name()).retrieveAll();
		List<Address> tempAddressList = retrieveAPI(EntityNames.ADDRESS.name()).retrieveAll();
		List<Location> tempLocationList = retrieveAPI(EntityNames.LOCATION.name()).retrieveAll();
		List<PeriodOfTime> tempPeriodOfTimeList = retrieveAPI(EntityNames.PERIODOFTIME.name()).retrieveAll();
		List<Identifier> tempIdentifierList = retrieveAPI(EntityNames.IDENTIFIER.name()).retrieveAll();
		List<Mapping> tempMappingList = retrieveAPI(EntityNames.MAPPING.name()).retrieveAll();
		List<Facility> tempFacilityList = retrieveAPI(EntityNames.FACILITY.name()).retrieveAll();
		List<Equipment> tempEquipmentList = retrieveAPI(EntityNames.EQUIPMENT.name()).retrieveAll();
		List<Plugin> tempPlugins = new ArrayList<>();

		try {
			Map<String, Object> params = new HashMap<>();
			params.put("plugins", "all");
			Response conversionResponse = doRequest(ServiceType.METADATA, Actor.getInstance(BuiltInActorType.CONVERTER), params);
			if (conversionResponse != null && conversionResponse.getPayloadAsPlainText().isPresent()) {
				tempPlugins = Arrays.stream(Utils.gson.fromJson(conversionResponse.getPayloadAsPlainText().get(), Plugin[].class)).collect(Collectors.toList());
			}
		}catch (Exception e) {
			System.err.println("[CONNECTION ERROR] Router not initializated, unable to retrieve information from the converter service. Stack:\n"+e.getMessage());
		}

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
		plugins = tempPlugins;

	}

	private static DatabaseConnections connections;

	public static DatabaseConnections getInstance() {
		if(connections==null) {
			connections = new DatabaseConnections();
		}
		if(connections.router==null){
			try {
				connections.router = RpcRouterBuilder.instance(Actor.getInstance(BuiltInActorType.TCS_CONNECTOR.verbLabel()).get())
						.addServiceSupport(ServiceType.EXTERNAL, Actor.getInstance(BuiltInActorType.CONVERTER.verbLabel()).get())
						.setNumberOfPublishers(1)
						.setNumberOfConsumers(1)
						.setRoutingKeyPrefix("resources")
						.build().get();
				System.out.println("[CONNECTION] Router initialized");
			}catch (Exception e) {
				System.err.println("[CONNECTION ERROR] Router not initializated, unable to retrieve information from the converter service. Stack:\n"+e.getMessage());
			}
		}
		return connections;
	}

	public List<DataProduct> getDataproducts() {
		return dataproducts;
	}

	public List<SoftwareApplication> getSoftwareApplications() {
		return softwareApplications;
	}

	public List<Organization> getOrganizationList() {
		return organizationList;
	}

	public List<Category> getCategoryList() {
		return categoryList;
	}

	public List<CategoryScheme> getCategorySchemesList() {return categorySchemesList;}

	public List<Distribution> getDistributionList() {
		return distributionList;
	}

	public List<Operation> getOperationList() {
		return operationList;
	}

	public List<WebService> getWebServiceList() {
		return webServiceList;
	}

	public List<Address> getAddressList() {
		return addressList;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public List<PeriodOfTime> getPeriodOfTimeList() {
		return periodOfTimeList;
	}

	public List<Identifier> getIdentifierList() {
		return identifierList;
	}

	public List<Mapping> getMappingList() { return mappingList;}

	public List<Facility> getFacilityList() { return facilityList;}

	public List<Equipment> getEquipmentList() { return equipmentList;}

	public List<Plugin> getPlugins() { return plugins;}

	protected Response doRequest(ServiceType service, Map<String, Object> requestParams) {
		return this.doRequest(service, null, requestParams);
	}

	protected Response doRequest(ServiceType service, Actor nextComponentOverride, Map<String, Object> requestParams)
	{

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

}
