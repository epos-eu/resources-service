import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.distributions.DistributionDetailsGenerationJPA;
import org.epos.api.core.distributions.DistributionSearchGenerationJPA;
import org.epos.api.core.organizations.OrganisationsGeneration;
import org.epos.api.facets.Facets;
import com.google.gson.Gson;

public class Tests {

	public static void main(String[] args) {
		
		Gson gson = new Gson();
		
		Facets.getInstance();
		if(EnvironmentVariables.MONITORING.equals("true")) {
			ZabbixExecutor.getInstance();
		}
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("id", "c1ad29f8-4f75-4095-8bb3-8f79caa76f7d");
		//headers.put("country", "IT");
		
		try {
			System.out.println(Facets.getInstance().generateFacetsFromDatabase());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(gson.toJsonTree(DistributionDetailsGenerationJPA.generate(headers)));

		//EntityManager em = new DBService().getEntityManager();
		//List<EDMFacility> facilities  = getFromDB(em, EDMFacility.class, "facility.findAllByState", "STATE", "PUBLISHED");
		
		/*facilities.forEach(fac ->{
			System.out.println(fac.getEdmEntityIdByOwner());
			fac.getEdmEntityIdByOwner().getOrganizationsByMetaId().forEach(aff->{
				System.out.println(aff.getUid());
			});
		});*/

		
		//System.out.println(FacetsGeneration.generateOnlyFacetsTree(new ArrayList<DiscoveryItem>()));
		
		//headers.put("id", "ad35e9db-d16f-4685-80f0-c23f64de8272");
		
		//FacilityDetailsItemGenerationJPA.generate(headers);
	}


}
