package org.epos.api.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.epos.api.beans.DataServiceProvider;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.model.EDMEdmEntityId;
import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationLegalname;

public class DataServiceProviderGeneration {

	public static List<DataServiceProvider> getProviders(List<EDMEdmEntityId> organizationsCollection) {
		List<EDMOrganization> organizations = new ArrayList<>();
		for (EDMEdmEntityId edmMetaId : organizationsCollection) {
			if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
				ArrayList<EDMOrganization> list = edmMetaId.getOrganizationsByMetaId().stream()
						.filter(e -> e.getState().equals(State.PUBLISHED.toString()))
						.collect(Collectors.toCollection(ArrayList::new));
				organizations.addAll(list);
			}
		}
		List<DataServiceProvider> organizationStructure = new ArrayList<>();
		for (EDMOrganization org : organizations) {

			// only take into account the organization with legalname
			if (org.getOrganizationLegalnameByInstanceId() != null && !org.getOrganizationLegalnameByInstanceId().isEmpty()) {

				String mainOrganizationLegalName;
				List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

				mainOrganizationLegalName = org.getOrganizationLegalnameByInstanceId().stream()
						.map(EDMOrganizationLegalname::getLegalname)
						.collect(Collectors.joining("."));

				if (Objects.nonNull(org.getSon()) && !org.getSon().isEmpty()) {
					relatedOrganizations.addAll(
							org.getSon().stream()
							.filter(relatedOrganization ->
							relatedOrganization.getOrganizationLegalnameByInstanceId() != null &&
							!relatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty())
							.map(relatedOrganization -> {

								String relatedOrganizationLegalName = relatedOrganization.getOrganizationLegalnameByInstanceId()
										.stream().map(EDMOrganizationLegalname::getLegalname)
										.collect(Collectors.joining("."));
								DataServiceProvider relatedDataprovider = new DataServiceProvider();
								relatedDataprovider.setDataProviderLegalName(relatedOrganizationLegalName);
								relatedDataprovider.setDataProviderUrl(relatedOrganization.getUrl());
								relatedDataprovider.setUid(relatedOrganization.getInstanceId());
								relatedDataprovider.setInstanceid(relatedOrganization.getInstanceId());
								relatedDataprovider.setMetaid(relatedOrganization.getInstanceId());
								if(relatedOrganization.getAddressByAddressId()!=null)relatedDataprovider.setCountry(relatedOrganization.getAddressByAddressId().getCountry());
								return relatedDataprovider;

							})
							.collect(Collectors.toList())
							);
					relatedOrganizations.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
				}

				DataServiceProvider dataServiceProvider = new DataServiceProvider();
				dataServiceProvider.setDataProviderLegalName(mainOrganizationLegalName);
				dataServiceProvider.setRelatedDataProvider(relatedOrganizations);
				dataServiceProvider.setDataProviderUrl(org.getUrl());
				dataServiceProvider.setUid(org.getInstanceId());
				dataServiceProvider.setInstanceid(org.getInstanceId());
				dataServiceProvider.setMetaid(org.getInstanceId());
				if(org.getAddressByAddressId()!=null) dataServiceProvider.setCountry(org.getAddressByAddressId().getCountry());

				organizationStructure.add(dataServiceProvider);
			}
		}


		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}
}
