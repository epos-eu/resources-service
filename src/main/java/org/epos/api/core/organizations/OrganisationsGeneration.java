package org.epos.api.core.organizations;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import metadataapis.OrganizationAPI;
import metadataapis.WebServiceAPI;
import model.EdmEntityId;
import model.StatusType;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.OrganizationBean;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.filtersearch.OrganizationFilterSearch;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class OrganisationsGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationsGeneration.class);


	public static List<OrganizationBean> generate(Map<String,Object> parameters) {

		LOGGER.info("Requests start - JPA method");

		List<Organization> organisations;

		if(parameters.containsKey("id")) {
			organisations = DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization -> organization.getInstanceId().equals(parameters.get("id").toString())).collect(Collectors.toList());
		}else {
			organisations = DatabaseConnections.getInstance().getOrganizationList();
			
			if(parameters.containsKey("type")) {
				List<Distribution> distributions = DatabaseConnections.getInstance().getDistributionList();

				List<Organization> tempOrganizationList;

				Set<Organization> organizationsEntityIds = new HashSet<>();

				distributions.forEach(distribution -> {
					if(parameters.get("type").toString().toLowerCase().contains("dataproviders")) {
						if(distribution.getDataProduct()!=null){
							for (LinkedEntity dataproduct : distribution.getDataProduct()) {
								Optional<DataProduct> dataProduct = DatabaseConnections.getInstance().getDataproducts().stream().filter(dataProduct1 -> dataProduct1.getInstanceId().equals(dataproduct.getInstanceId())).findFirst();
								if(dataProduct.isPresent()){
									if(dataProduct.get().getPublisher()!=null)
										dataProduct.get().getPublisher().forEach(publisher -> {
											Optional<Organization> organization = DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization1 -> organization1.getInstanceId().equals(publisher.getInstanceId())).findFirst();
                                            organization.ifPresent(organizationsEntityIds::add);
										});
								}
							}
						}
					}
					if(parameters.get("type").toString().toLowerCase().contains("serviceproviders") && distribution.getAccessService()!=null) {
						if(distribution.getAccessService()!=null){
							for (LinkedEntity webservice : distribution.getAccessService()) {
								Optional<WebService> webService = DatabaseConnections.getInstance().getWebServiceList().stream().filter(webService1 -> webService1.getInstanceId().equals(webservice.getInstanceId())).findFirst();
								if(webService.isPresent()){
									if(webService.get().getProvider()!=null){
										Optional<Organization> organization = DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization1 -> organization1.getInstanceId().equals(webService.get().getProvider().getInstanceId())).findFirst();
										organization.ifPresent(organizationsEntityIds::add);
									}
								}
							}
						}
					}
				});
				if(parameters.get("type").toString().toLowerCase().contains("facilitiesproviders")) {
                    organizationsEntityIds.addAll(organisations.stream()
                            .filter(org -> !org.getOwns().isEmpty())
                            .collect(Collectors.toList()));
				}
				
				List<DataServiceProvider> providers = DataServiceProviderGeneration.getProviders(new ArrayList<Organization>(organizationsEntityIds));
				List<String> providersInstanceIds = new ArrayList<String>();
				providers.forEach(resource->{
					providersInstanceIds.add(resource.getInstanceid());

					resource.getRelatedDataProvider().forEach(relatedData ->{
						providersInstanceIds.add(relatedData.getInstanceid());
					});
					resource.getRelatedDataServiceProvider().forEach(relatedDataService ->{
						providersInstanceIds.add(relatedDataService.getInstanceid());
					});
				});
				
				tempOrganizationList = organisations.stream().filter(org -> providersInstanceIds.contains(org.getInstanceId())).collect(Collectors.toList());
				organisations = tempOrganizationList;
			}

			System.out.println(organisations.size());

			LOGGER.info("Apply filter using input parameters: "+parameters.toString());
			organisations = OrganizationFilterSearch.doFilters(organisations, parameters);

			System.out.println(organisations.size());


			
			if(parameters.containsKey("country")){
				List<Organization> tempOrganizationList = new ArrayList<>();
				for(Organization e : organisations) {
					if(e.getAddress()!=null){
						Optional<Address> address = DatabaseConnections.getInstance().getAddressList().stream().filter(address1 -> address1.getInstanceId().equals(e.getAddress().getInstanceId())).findFirst();
						if(address.isPresent() && address.get().getCountry().equals(parameters.get("country"))){
							tempOrganizationList.add(e);
						}
					}
				}
				organisations = tempOrganizationList;
			}
		}
		
		List<OrganizationBean> organisationsReturn = new ArrayList<OrganizationBean>();

		for(Organization singleOrganization : organisations) {
			if(singleOrganization.getLegalName()!=null){
				String legalName = String.join(";", singleOrganization.getLegalName());
				Address address = null;
				if(singleOrganization.getAddress()!=null) {
					Optional<Address> addressOptional = DatabaseConnections.getInstance().getAddressList().stream().filter(address1 -> address1.getInstanceId().equals(singleOrganization.getAddress().getInstanceId())).findFirst();
					if(addressOptional.isPresent()){
						address = addressOptional.get();
					}
				}
				OrganizationBean ob = new OrganizationBean(singleOrganization.getInstanceId(), singleOrganization.getLogo(), singleOrganization.getURL(), legalName,address!=null? address.getCountry() : null);
				organisationsReturn.add(ob);
			}
		}

		return organisationsReturn;

	}

}
