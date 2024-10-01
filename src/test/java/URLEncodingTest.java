import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import org.epos.api.core.URLGeneration;

public class URLEncodingTest {

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		
		String template = "https://episodesplatform.eu/api/epos/episode-elements{?episode,element}";
		
		
		HashMap<String,Object> parameters = new HashMap<>();
		parameters.put("episode", "ASPO");
		parameters.put("element", "separated value");
		
		String urlStr = URLGeneration.generateURLFromTemplateAndMap(template, parameters);
		URL url = new URL(urlStr);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

		urlStr = uri.toASCIIString();

		System.out.println("OUTPUT: "+urlStr);
		
	}


}
