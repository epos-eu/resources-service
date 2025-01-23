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

public class DetailsWebServiceTest extends TestcontainersLifecycle {

    @Test
    @Order(1)
    public void testDetailsWebService() throws InterruptedException, IOException {

        ScheduledRuntimes scheduledRuntimes = new ScheduledRuntimes();
        scheduledRuntimes.onStartup();

        Map<String, Object> params = new HashMap<>();
        params.put("id", "adcd7794-2daf-44be-af55-88273564b8e1");


        Distribution response = DistributionDetailsGenerationJPA.generate(params);

        System.out.println("Details response: " + response);

    }

}
