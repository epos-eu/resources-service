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

public class DetailsDownloadableFileTest extends TestcontainersLifecycle {

    @Test
    @Order(1)
    public void testDetailsDownloadableFile() throws InterruptedException, IOException {

        ScheduledRuntimes scheduledRuntimes = new ScheduledRuntimes();
        scheduledRuntimes.onStartup();

        Map<String, Object> params = new HashMap<>();
        params.put("id", "d56707f6-fd1d-49a8-9af4-8e68ba6d77a8");


        Distribution response = DistributionDetailsGenerationJPA.generate(params);

        System.out.println("Details response: " + response);

    }
}
