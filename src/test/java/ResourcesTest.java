import java.util.HashMap;
import java.util.Map;

import org.epos.api.ClientHelpersApiController;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.SearchGenerationJPA;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.facets.Facets;
import org.epos.api.utility.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourcesTest {

	public static void main(String[] args) {

		Facets.getInstance();
		ZabbixExecutor.getInstance();

		SearchResponse response = SearchGenerationJPA.generate(new HashMap<String,Object>());
		
		System.out.println(response);
	}

}
