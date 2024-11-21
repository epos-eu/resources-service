package org.epos.api.core;

import java.util.*;
import java.util.stream.Collectors;

import commonapis.LinkedEntityAPI;
import io.swagger.v3.oas.models.links.Link;
import model.StatusType;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.Address;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Organization;

public class DataServiceProviderGeneration {

	public static List<DataServiceProvider> getProviders(List<Organization> organizations) {

		List<DataServiceProvider> organizationStructure = new ArrayList<>();
		for (Organization org : organizations) {
			if (org != null) {
				// only take into account the organization with legalname
				if (org.getLegalName() != null && !org.getLegalName().isEmpty()) {

					String mainOrganizationLegalName;
					List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

					mainOrganizationLegalName = String.join(".", org.getLegalName());

					if (org.getMemberOf() == null) {
						DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization -> organization.getMemberOf() != null)
								.forEach(organization1 -> {
									Optional<LinkedEntity> resultEntities = organization1.getMemberOf().stream()
											.filter(linkedEntity -> linkedEntity.getInstanceId().equals(org.getInstanceId()))
											.findAny();

									if (resultEntities.isPresent()) {
										if (organization1.getLegalName() != null && !organization1.getLegalName().isEmpty()) {
											String relatedOrganizationLegalName = String.join(".", organization1.getLegalName());
											DataServiceProvider relatedDataprovider = new DataServiceProvider();
											relatedDataprovider.setDataProviderLegalName(relatedOrganizationLegalName);
											relatedDataprovider.setDataProviderUrl(organization1.getURL());
											relatedDataprovider.setUid(organization1.getInstanceId());
											relatedDataprovider.setInstanceid(organization1.getInstanceId());
											relatedDataprovider.setMetaid(organization1.getInstanceId());
											if (organization1.getAddress() != null) {
												Optional<Address> address = DatabaseConnections.getInstance().getAddressList().stream().filter(address1 -> address1.getInstanceId().equals(organization1.getAddress().getInstanceId())).findFirst();
												address.ifPresent(value -> relatedDataprovider.setCountry(value.getCountry()));
											}
											relatedOrganizations.add(relatedDataprovider);
										}
									}
								});
					}
					relatedOrganizations.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));

					DataServiceProvider dataServiceProvider = new DataServiceProvider();
					dataServiceProvider.setDataProviderLegalName(mainOrganizationLegalName);
					dataServiceProvider.setRelatedDataProvider(relatedOrganizations);
					dataServiceProvider.setDataProviderUrl(org.getURL());
					dataServiceProvider.setUid(org.getInstanceId());
					dataServiceProvider.setInstanceid(org.getInstanceId());
					dataServiceProvider.setMetaid(org.getInstanceId());
					if (org.getAddress() != null) {
						Optional<Address> address = DatabaseConnections.getInstance().getAddressList().stream().filter(address1 -> address1.getInstanceId().equals(org.getAddress().getInstanceId())).findFirst();
						address.ifPresent(value -> dataServiceProvider.setCountry(value.getCountry()));
					}
					organizationStructure.add(dataServiceProvider);
				}
			}
		}

		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}
}
