import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.epos.api.beans.DiscoveryItem;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.distributions.DistributionDetailsGenerationJPA;
import org.epos.api.core.distributions.DistributionSearchGenerationJPA;
import org.epos.api.core.facilities.FacilityDetailsItemGenerationJPA;
import org.epos.api.core.facilities.FacilitySearchGenerationJPA;
import org.epos.api.core.organizations.OrganisationsGeneration;
import org.epos.api.facets.Facets;
import org.epos.api.facets.FacetsGeneration;
import org.epos.api.facets.FacetsNodeTree;
import org.epos.handler.dbapi.model.EDMFacility;
import org.epos.handler.dbapi.service.DBService;

import com.google.gson.Gson;
import com.jayway.jsonpath.internal.Utils;

public class Tests {

	public static void main(String[] args) {
		
		Gson gson = new Gson();
		
		Facets.getInstance();
		if(EnvironmentVariables.MONITORING.equals("true")) {
			ZabbixExecutor.getInstance();
		}
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("type", "facilitiesproviders");
		
		try {
			System.out.println(Facets.getInstance().generateFacetsFromDatabase());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(gson.toJsonTree(OrganisationsGeneration.generate(headers)));

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
