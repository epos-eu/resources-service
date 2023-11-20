import java.util.Map;

import org.epos.api.ClientHelpersApiController;
import org.epos.api.facets.Facets;
import org.epos.api.utility.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourcesTest {

	public static void main(String[] args) {

		Facets.getInstance();

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//SearchResponse response = SearchGenerationJPA.generate(new HashMap<String,Object>());

		Map<String,Object> requestParameters = Map.of("id", "b504afe2-a949-47aa-8b28-4587f6b5a84f");
		
		
		ClientHelpersApiController chac = new ClientHelpersApiController(new ObjectMapper(), null);
		
		System.out.println(Utils.gson.toJsonTree(chac.resourcesDiscoveryGetUsingGET("e085606e-38a7-452f-99b9-623f71de9e9e")));

		//Distribution response = DetailsItemGenerationJPA.generate(requestParameters);

		/*try {
		System.out.println(ResponseEntity.ok(response));
		}catch(Exception e) {
			e.printStackTrace();
		}*/
	}

}
