import java.util.HashMap;
import java.util.Map;

import org.epos.api.core.DetailsItemGenerationJPA;
import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.SearchGenerationJPA;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.facets.Facets;

public class Tests {

	public static void main(String[] args) {
		
		Facets.getInstance();
		if(EnvironmentVariables.MONITORING.equals("true")) {
			ZabbixExecutor.getInstance();
		}
		
		Map<String, Object> headers = new HashMap<String, Object>();
		
		SearchGenerationJPA.generate(headers);
		
		//headers.put("id", "f42f11c2-1c38-404d-a204-a23259be73aa");
		
		//DetailsItemGenerationJPA.generate(headers);
	}


}
