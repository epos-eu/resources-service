import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
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
		headers.put("id", "all");
		headers.put("facilityid", "a3da9c4a-f73f-41d2-b414-9a15d3078076");
		headers.put("format", "application/epos.geo+json");
		headers.put("params", "{\"equipmenttypes\":\"02a5bffb-9a4d-4a69-b0fb-9b15e831deb3\"}");
		//headers.put("format", "application/epos.geo+json");
		
		//FacilityDetailsItemGenerationJPA.generate(headers);
		System.out.println(gson.toJsonTree(EquipmentsDetailsItemGenerationJPA.generate(headers)));

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
