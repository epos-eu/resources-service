import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.epos.api.beans.DataServiceProvider;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.model.EDMEdmEntityId;
import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationLegalname;
import org.epos.handler.dbapi.service.DBService;

public class ResourcesTest {

	public static void main(String[] args) throws IOException {

		//Facets.getInstance();
		//ZabbixExecutor.getInstance();

		//SearchResponse response = SearchGenerationJPA.generate(new HashMap<String,Object>());

		//System.out.println(response);


		EntityManager em = new DBService().getEntityManager();

		List<EDMOrganization> organizations = getFromDB(em, EDMOrganization.class, "organization.findAllByState", "STATE", "PUBLISHED");
		
		em.close();
		
		List<DataServiceProvider> providers = ResourcesTest.getProviders(organizations);
		
		FileWriter fw = new FileWriter("organiz.csv");

		fw.write("ORGANIZATION NAME | ORGANIZATION URL | PARENT NAME\n");
		
		providers.forEach(pro->{
			
			try {
				fw.write(pro.getDataProviderLegalName()+" | "+pro.getDataProviderUrl()+"|\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pro.getRelatedDataProvider().forEach(son->{
				try {
					fw.write(son.getDataProviderLegalName()+" | "+son.getDataProviderUrl()+"|"+pro.getDataProviderLegalName()+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
		});

		fw.close();
		
		
	}
	
	public static List<DataServiceProvider> getProviders(List<EDMOrganization> organizationsCollection) {
		List<EDMOrganization> organizations = new ArrayList<>();
		for (EDMOrganization edmMetaIdOrg : organizationsCollection) {
			EDMEdmEntityId edmMetaId = edmMetaIdOrg.getEdmEntityIdByMetaId();
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
				if(org.getAddressByAddressId()!=null) dataServiceProvider.setCountry(org.getAddressByAddressId().getCountry());

				organizationStructure.add(dataServiceProvider);

			}

		}


		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}

}
