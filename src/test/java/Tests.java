import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.distributions.DistributionSearchGenerationJPA;
import org.epos.api.core.facilities.EquipmentsDetailsItemGenerationJPA;
import org.epos.api.core.facilities.FacilityDetailsItemGenerationJPA;
import org.epos.api.core.facilities.FacilitySearchGenerationJPA;
import org.epos.api.core.organizations.OrganisationsGeneration;
import org.epos.api.facets.Facets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Tests {

	public static void main(String[] args) {
		
		Gson gson = new Gson();
		JsonObject facetsFromDatabase;
		try {
			facetsFromDatabase = Facets.getInstance().generateFacetsFromDatabase();
			Facets.getInstance().setFacetsFromDatabase(facetsFromDatabase);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Object> headers = new HashMap<String, Object>();
		//headers.put("id", "all");
		//headers.put("facilityid", "e181b188-e4d6-4c85-a4d1-f212fb253661");
		//headers.put("format", "application/epos.geo+json");
		//headers.put("params", "{\"equipmenttypes\":}");
		//headers.put("format", "application/epos.geo+json");
		
		//FacilityDetailsItemGenerationJPA.generate(headers);
		System.out.println(gson.toJsonTree(DistributionSearchGenerationJPA.generate(headers, null)));

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
