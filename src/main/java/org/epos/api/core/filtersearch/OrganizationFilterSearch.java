package org.epos.api.core.filtersearch;

import java.util.*;
import java.util.stream.Collectors;

import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationIdentifier;
import org.epos.handler.dbapi.model.EDMOrganizationLegalname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrganizationFilterSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationFilterSearch.class); 

	public static List<EDMOrganization> doFilters(List<EDMOrganization> organisationsList, Map<String,Object> parameters) {

		organisationsList = filterOrganisationsByFullText(organisationsList, parameters);
		organisationsList = filterOrganisationsByCountry(organisationsList, parameters);

		return organisationsList;
	}

	private static List<EDMOrganization> filterOrganisationsByFullText(List<EDMOrganization> organisationsList, Map<String,Object> parameters) {
		if(parameters.containsKey("q")) {
			HashSet<EDMOrganization> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").toString().toLowerCase().split(",");

			for (EDMOrganization edmOrganisation : organisationsList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));

				if (edmOrganisation.getOrganizationLegalnameByInstanceId() != null && !edmOrganisation.getOrganizationLegalnameByInstanceId().isEmpty()) {
					for (EDMOrganization edmOrganisation1 : edmOrganisation.getOrganizationLegalnameByInstanceId().stream().map(EDMOrganizationLegalname::getOrganizationIdByInstanceOrganizationId).collect(Collectors.toList())) {

						//distribution title
						if (edmOrganisation1.getOrganizationLegalnameByInstanceId() != null) {
							edmOrganisation1.getOrganizationLegalnameByInstanceId()
							.stream().map(EDMOrganizationLegalname::getLegalname)
							.forEach(title ->
							{
								for (String q : qSMap.keySet()) {
									if (title.toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							});
						}
					}
				}
				
				if(Objects.nonNull(edmOrganisation.getOrganizationIdentifiersByInstanceId())){
					for(EDMOrganizationIdentifier identifier : edmOrganisation.getOrganizationIdentifiersByInstanceId()) {
						for (String q : qSMap.keySet()) {
							if (identifier.getIdentifier().contains(q)) qSMap.put(q, Boolean.TRUE);
							if (identifier.getType().contains(q)) qSMap.put(q, Boolean.TRUE);
						}
					}
					
				}

				
				if(Objects.nonNull(edmOrganisation.getUid())){
					for (String q : qSMap.keySet()) {
						if (edmOrganisation.getUid().contains(q)) qSMap.put(q, Boolean.TRUE);
					}
				}

				//take a dataproduct onlly if every element of the freetext search is satisfied
				if (qSMap.values().stream().allMatch(b -> b)) tempDatasetList.add(edmOrganisation);
			}

			organisationsList = new ArrayList<>(tempDatasetList);
		}

		return organisationsList;

	}
	
	private static List<EDMOrganization> filterOrganisationsByCountry(List<EDMOrganization> organisationsList, Map<String,Object> parameters) {
		if(parameters.containsKey("country")) {
			HashSet<EDMOrganization> tempOrganizationList = new HashSet<>();
			String[] qs = parameters.get("country").toString().toLowerCase().split(",");

			for (EDMOrganization edmOrganisation : organisationsList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));
				for (String q : qSMap.keySet()) {
					if(edmOrganisation.getAddressByAddressId()!=null && edmOrganisation.getAddressByAddressId().getCountry()!=null && q.equals(edmOrganisation.getAddressByAddressId().getCountry().toLowerCase())) {
						tempOrganizationList.add(edmOrganisation);
					}
				}
			}

			organisationsList = new ArrayList<>(tempOrganizationList);
		}

		return organisationsList;

	}


}
