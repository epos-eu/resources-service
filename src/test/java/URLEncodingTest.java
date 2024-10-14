import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import org.epos.api.core.URLGeneration;

public class URLEncodingTest {

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		
		String template = "https://episodesplatform.eu/api/epos/episode-elements{?episode,element,before}";
		
		
		HashMap<String,Object> parameters = new HashMap<>();
		parameters.put("episode", URLGeneration.encodeValue("ASPO"));
		parameters.put("element", URLGeneration.encodeValue("separated value"));
		parameters.put("before", URLGeneration.encodeValue("2013-02-26"));
		
		String compiledUrl = URLGeneration.generateURLFromTemplateAndMap(template, parameters);
		//URL url = new URL(urlStr);
		//URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

		//urlStr = uri.toASCIIString();
		
		try {
			compiledUrl = URLGeneration.ogcWFSChecker(compiledUrl);
		}catch(Exception e) {
			System.out.println("Found the following issue whilst executing the WFS Checker, issue raised "+ e.getMessage() + " - Continuing execution");
		}
		

		System.out.println("OUTPUT: "+compiledUrl);
		
	}


}
