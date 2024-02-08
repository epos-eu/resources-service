package org.epos.api.core.filtersearch;

import java.util.*;
import java.util.stream.Collectors;

import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationLegalname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrganizationFilterSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationFilterSearch.class); 

	public static List<EDMOrganization> doFilters(List<EDMOrganization> organisationsList, Map<String,Object> parameters) {

		organisationsList = filterOrganisationsByFullText(organisationsList, parameters);
		//organisationsList = filterOrganisationsByType(organisationsList, parameters);

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

				//take a dataproduct onlly if every element of the freetext search is satisfied
				if (qSMap.values().stream().allMatch(b -> b)) tempDatasetList.add(edmOrganisation);
			}

			organisationsList = new ArrayList<>(tempDatasetList);
		}

		return organisationsList;

	}


}
