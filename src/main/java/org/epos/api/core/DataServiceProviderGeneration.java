package org.epos.api.core;

import java.util.*;

import abstractapis.AbstractAPI;
import metadataapis.EntityNames;
import org.epos.api.beans.DataServiceProvider;
import org.epos.api.routines.DatabaseConnections;
import org.epos.eposdatamodel.Address;
import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Organization;

public class DataServiceProviderGeneration {

	public static List<DataServiceProvider> getProviders(List<Organization> organizations) {

		List<Organization> organizationList = (List<Organization>) AbstractAPI.retrieveAPI(EntityNames.ORGANIZATION.name()).retrieveAll();

		List<DataServiceProvider> organizationStructure = new ArrayList<>();
		for (Organization org : organizations) {
			if (org != null) {
				// only take into account the organization with legalname
				if (org.getLegalName() != null && !org.getLegalName().isEmpty()) {

					String mainOrganizationLegalName;
					List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

					mainOrganizationLegalName = String.join(".", org.getLegalName());

					if (org.getMemberOf() == null) {
						organizationList.stream().filter(organization -> organization.getMemberOf() != null)
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
												Address address = (Address) AbstractAPI.retrieveAPI(EntityNames.ADDRESS.name()).retrieve(organization1.getAddress().getInstanceId());
												if(Objects.nonNull(address) && Objects.nonNull(address.getCountry())) relatedDataprovider.setCountry(address.getCountry());
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
						Address address = (Address) AbstractAPI.retrieveAPI(EntityNames.ADDRESS.name()).retrieve(org.getAddress().getInstanceId());
						if(Objects.nonNull(address) && Objects.nonNull(address.getCountry())) dataServiceProvider.setCountry(address.getCountry());
					}
					organizationStructure.add(dataServiceProvider);
				}
			}
		}

		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}
}
