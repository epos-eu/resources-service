import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.facilities.EquipmentsDetailsItemGenerationJPA;
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
		headers.put("id", "fcab79ad-f540-4ccd-9b62-2e37963f06c2");
		headers.put("format", "application/epos.geo+json");
		
		try {
			System.out.println(Facets.getInstance().generateFacetsFromDatabase());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
