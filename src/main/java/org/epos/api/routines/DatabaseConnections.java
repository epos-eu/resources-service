package org.epos.api.routines;


import commonapis.AddressAPI;
import commonapis.IdentifierAPI;
import commonapis.SpatialAPI;
import commonapis.TemporalAPI;
import metadataapis.*;
import model.Dataproduct;
import org.epos.eposdatamodel.*;
import org.epos.handler.dbapi.service.EntityManagerService;

import java.util.List;

public class DatabaseConnections {

	private final DataProductAPI dataProductAPI = new DataProductAPI(EntityNames.DATAPRODUCT.name(), Dataproduct.class);
	private final DistributionAPI distributionAPI = new DistributionAPI(EntityNames.DISTRIBUTION.name(), model.Distribution.class);
	private final OrganizationAPI organizationAPI = new OrganizationAPI(EntityNames.ORGANIZATION.name(), model.Organization.class);
	private final WebServiceAPI webServiceAPI = new WebServiceAPI(EntityNames.WEBSERVICE.name(), model.Webservice.class);
	private final OperationAPI operationAPI = new OperationAPI(EntityNames.OPERATION.name(), model.Operation.class);
	private final CategoryAPI categoryAPI = new CategoryAPI(EntityNames.CATEGORY.name(), model.Category.class);
	private final SoftwareApplicationAPI softwareApplicationAPI = new SoftwareApplicationAPI(EntityNames.SOFTWAREAPPLICATION.name(), model.Softwareapplication.class);
	private final AddressAPI addressAPI = new AddressAPI(EntityNames.ADDRESS.name(), model.Address.class);
	private final SpatialAPI spatialAPI = new SpatialAPI(EntityNames.LOCATION.name(), model.Spatial.class);
	private final TemporalAPI temporalAPI = new TemporalAPI(EntityNames.PERIODOFTIME.name(), model.Temporal.class);
	private final IdentifierAPI identifierAPI = new IdentifierAPI(EntityNames.IDENTIFIER.name(), model.Identifier.class);
	private final MappingAPI mappingAPI = new MappingAPI(EntityNames.MAPPING.name(), model.Mapping.class);
	private final FacilityAPI facilityAPI = new FacilityAPI(EntityNames.FACILITY.name(), model.Mapping.class);
	private final EquipmentAPI equipmentAPI = new EquipmentAPI(EntityNames.EQUIPMENT.name(), model.Mapping.class);


	private List<DataProduct> dataproducts;
	private List<SoftwareApplication> softwareApplications;
	private List<Organization> organizationList;
	private List<Category> categoryList;
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

	private DatabaseConnections() {}

	public void syncDatabaseConnections() {
		EntityManagerService.getInstance().getCache().evictAll();

		List<DataProduct> tempDataproducts  = dataProductAPI.retrieveAll();
		List<SoftwareApplication> tempSoftwareApplications = softwareApplicationAPI.retrieveAll();
		List<Organization> tempOrganizationList = organizationAPI.retrieveAll();
		List<Category> tempCategoryList = categoryAPI.retrieveAll();
		List<Distribution> tempDistributionList = distributionAPI.retrieveAll();
		List<Operation> tempOperationList = operationAPI.retrieveAll();
		List<WebService> tempWebServiceList = webServiceAPI.retrieveAll();
		List<Address> tempAddressList = addressAPI.retrieveAll();
		List<Location> tempLocationList = spatialAPI.retrieveAll();
		List<PeriodOfTime> tempPeriodOfTimeList = temporalAPI.retrieveAll();
		List<Identifier> tempIdentifierList = identifierAPI.retrieveAll();
		List<Mapping> tempMappingList = mappingAPI.retrieveAll();
		List<Facility> tempFacilityList = facilityAPI.retrieveAll();
		List<Equipment> tempEquipmentList = equipmentAPI.retrieveAll();

		dataproducts = tempDataproducts;
		softwareApplications = tempSoftwareApplications;
		organizationList = tempOrganizationList;
		categoryList = tempCategoryList;
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
	}

	private static DatabaseConnections connections;

	public static DatabaseConnections getInstance() {
		if(connections==null) connections = new DatabaseConnections();
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

}
