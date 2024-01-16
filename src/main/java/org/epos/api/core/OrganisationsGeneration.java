package org.epos.api.core;

import org.epos.api.beans.OrganizationBean;
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


			LOGGER.info("Apply filter using input parameters: "+parameters.toString());
			organisations = FilterSearch.doOrganisationsFilters(organisations, parameters);
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
