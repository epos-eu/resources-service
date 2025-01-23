import org.epos.api.beans.Distribution;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.distributions.DistributionDetailsGenerationJPA;
import org.epos.api.core.distributions.DistributionSearchGenerationJPA;
import org.epos.api.routines.ScheduledRuntimes;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class DataSearchTest extends TestcontainersLifecycle {

    @Test
    @Order(1)
    public void testSearch() throws InterruptedException, IOException {

        ScheduledRuntimes scheduledRuntimes = new ScheduledRuntimes();
        scheduledRuntimes.onStartup();

        Map<String, Object> params = new HashMap<>();
        params.put("facets", "true");
        params.put("facetstype", "categories");


        SearchResponse response = DistributionSearchGenerationJPA.generate(params, null);

        System.out.println("Search response: " + response.getResults());
        System.out.println("Search response: " + response.getFilters());

    }

}
