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
		headers.put("organisations", "897f2233-709e-4b47-8dda-9ac4e294979f");
		//headers.put("format", "application/epos.geo+json");
		
		//FacilityDetailsItemGenerationJPA.generate(headers);
		System.out.println(gson.toJsonTree(FacilitySearchGenerationJPA.generate(headers)));

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
