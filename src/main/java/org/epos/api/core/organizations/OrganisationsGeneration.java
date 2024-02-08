package org.epos.api.core.organizations;

import org.epos.api.beans.DataServiceProvider;
import org.epos.api.beans.NodeFilters;
import org.epos.api.beans.OrganizationBean;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.core.filtersearch.OrganizationFilterSearch;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class OrganisationsGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationsGeneration.class);


	public static List<OrganizationBean> generate(Map<String,Object> parameters) {

		LOGGER.info("Requests start - JPA method");
		EntityManager em = new DBService().getEntityManager();

		List<EDMOrganization> organisations  = null;

		if(parameters.containsKey("id")) {
			organisations = getFromDB(em, EDMOrganization.class, "organization.findByInstanceId", "INSTANCEID", parameters.get("id"));
		}else {
			organisations = getFromDB(em, EDMOrganization.class, "organization.findAllByState", "STATE", "PUBLISHED");
			
			if(parameters.containsKey("type")) {
				List<EDMDataproduct> dataproducts  = getFromDB(em, EDMDataproduct.class, "dataproduct.findAllByState", "STATE", "PUBLISHED");
				List<EDMOrganization> tempOrganizationList = new ArrayList<>();

				Set<EDMEdmEntityId> organizationsEntityIds = new HashSet<>();

				dataproducts.stream().forEach(dataproduct -> {
					List<EDMWebservice> webservices = dataproduct.getIsDistributionsByInstanceId().stream()
							.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
							.filter(Objects::nonNull)
							.map(EDMDistribution::getWebserviceByAccessService)
							.filter(Objects::nonNull)
							.collect(Collectors.toList());

					if(parameters.get("type").toString().toLowerCase().contains("dataproviders")) {
						dataproduct.getPublishersByInstanceId().stream()
						.map(EDMPublisher::getEdmEntityIdByMetaOrganizationId)
						.filter(Objects::nonNull)
						.collect(Collectors.toList())
						.forEach(organizationsEntityIds::add);
					}
					if(parameters.get("type").toString().toLowerCase().contains("serviceproviders")) {
						webservices.stream()
						.map(EDMWebservice::getEdmEntityIdByProvider)
						.filter(Objects::nonNull)
						.collect(Collectors.toList())
						.forEach(organizationsEntityIds::add);
					}
				});

				if(parameters.get("type").toString().toLowerCase().contains("facilitiesproviders")) {
					organisations.stream()
							.filter(org->!org.getOwnsByInstanceId().isEmpty())
							.map(EDMOrganization::getEdmEntityIdByMetaId)
							.collect(Collectors.toList())
							.forEach(organizationsEntityIds::add);
				}
				
				List<DataServiceProvider> providers = DataServiceProviderGeneration.getProviders(new ArrayList<EDMEdmEntityId>(organizationsEntityIds));
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


			LOGGER.info("Apply filter using input parameters: "+parameters.toString());
			organisations = OrganizationFilterSearch.doFilters(organisations, parameters);
			if(parameters.containsKey("country")) organisations = organisations.stream().filter(e->e.getAddressByAddressId()!=null && e.getAddressByAddressId().getCountry().equals(parameters.get("country"))).collect(Collectors.toList());
		}
		
		
		List<OrganizationBean> organisationsReturn = new ArrayList<OrganizationBean>();

		for(EDMOrganization singleOrganization : organisations) {
			String legalName = null;
			for(EDMOrganizationLegalname legalNameInstance : singleOrganization.getOrganizationLegalnameByInstanceId()) {
				legalName = legalNameInstance.getLegalname();
			}
			OrganizationBean ob = new OrganizationBean(singleOrganization.getInstanceId(), singleOrganization.getLogo(), singleOrganization.getUrl(), legalName,singleOrganization.getAddressByAddressId()!=null? singleOrganization.getAddressByAddressId().getCountry() : null);
			organisationsReturn.add(ob);
		}


		return organisationsReturn;

	}

}
