package org.epos.api.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import commonapis.LinkedEntityAPI;
import io.swagger.v3.oas.models.links.Link;
import model.StatusType;
import org.epos.api.beans.DataServiceProvider;
import org.epos.eposdatamodel.Address;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Organization;

public class DataServiceProviderGeneration {

	public static List<DataServiceProvider> getProviders(List<Organization> organizationsCollection) {
		List<Organization> organizations = new ArrayList<>();
		for (Organization edmMetaId : organizationsCollection) {
			if (edmMetaId.getMetaId() != null && !edmMetaId.getMetaId().isEmpty()) {
				if(edmMetaId.getStatus().equals(StatusType.PUBLISHED)) organizations.add(edmMetaId);
			}
		}
		List<DataServiceProvider> organizationStructure = new ArrayList<>();
		for (Organization org : organizations) {

			// only take into account the organization with legalname
			if (org.getLegalName() != null && !org.getLegalName().isEmpty()) {

				String mainOrganizationLegalName;
				List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

				mainOrganizationLegalName = String.join(".", org.getLegalName());

				if (Objects.nonNull(org.getMemberOf()) && !org.getMemberOf().isEmpty()) {
					relatedOrganizations.addAll(
							org.getMemberOf().stream()
							.map(relatedOrganizationLinkedEntity -> {
								Organization relatedOrganization = (Organization) LinkedEntityAPI.retrieveFromLinkedEntity(relatedOrganizationLinkedEntity);
								if (org.getLegalName() != null && !org.getLegalName().isEmpty()) {
									String relatedOrganizationLegalName = String.join(".", relatedOrganization.getLegalName());
									DataServiceProvider relatedDataprovider = new DataServiceProvider();
									relatedDataprovider.setDataProviderLegalName(relatedOrganizationLegalName);
									relatedDataprovider.setDataProviderUrl(relatedOrganization.getURL());
									relatedDataprovider.setUid(relatedOrganization.getInstanceId());
									relatedDataprovider.setInstanceid(relatedOrganization.getInstanceId());
									relatedDataprovider.setMetaid(relatedOrganization.getInstanceId());
									if (relatedOrganization.getAddress() != null) {
										Address address = (Address) LinkedEntityAPI.retrieveFromLinkedEntity(relatedOrganization.getAddress());
										relatedDataprovider.setCountry(address.getCountry());
									}
									return relatedDataprovider;
								}
                                return null;
                            })
							.collect(Collectors.toList())
							);
					relatedOrganizations.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
				}

				DataServiceProvider dataServiceProvider = new DataServiceProvider();
				dataServiceProvider.setDataProviderLegalName(mainOrganizationLegalName);
				dataServiceProvider.setRelatedDataProvider(relatedOrganizations);
				dataServiceProvider.setDataProviderUrl(org.getURL());
				dataServiceProvider.setUid(org.getInstanceId());
				dataServiceProvider.setInstanceid(org.getInstanceId());
				dataServiceProvider.setMetaid(org.getInstanceId());
				if (org.getAddress() != null) {
					Address address = (Address) LinkedEntityAPI.retrieveFromLinkedEntity(org.getAddress());
					dataServiceProvider.setCountry(address.getCountry());
				}

				organizationStructure.add(dataServiceProvider);
			}
		}


		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}
}
