import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.epos.api.core.MonitoringGeneration;
import org.epos.api.core.URLGeneration;
import org.epos.api.utility.Utils;

public class MonitoringCallTest {

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		
		System.out.println(Utils.gson.toJson(MonitoringGeneration.generate()));
		//System.out.println(MonitoringGeneration.generate());
		//MonitoringGeneration.generate();
	}


}
