import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.epos.api.ClientHelpersApiController;
import org.epos.api.beans.SearchResponse;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.core.distributions.DistributionSearchGenerationJPA;
import org.epos.api.facets.Facets;
import org.epos.api.utility.Utils;
import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.service.DBService;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OrganizationList {

	public static void main(String[] args) throws IOException {

		EntityManager em = new DBService().getEntityManager();

		List<EDMOrganization> organizations = getFromDB(em, EDMOrganization.class, "organization.findAllByState", "STATE", "PUBLISHED");

		em.close();

		FileWriter fw = new FileWriter("organizations.csv");

		fw.write("ORGANIZATION NAME | ORGANIZATION URL | PARENT NAME\n");

		organizations.forEach(orga->{
			if(orga.getSon().isEmpty()) {
				orga.getOrganizationLegalnameByInstanceId().forEach(nm->{
					try {
						fw.write(nm.getLegalname()+" | "+orga.getUrl()+"|\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} else {
				orga.getSon().forEach(son -> {
					son.getOrganizationLegalnameByInstanceId().forEach(nm->{
						orga.getOrganizationLegalnameByInstanceId().forEach(nm2->{
							try {
								fw.write(nm.getLegalname()+" | "+son.getUrl()+"|"+nm2.getLegalname()+"\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});

					});
				});
			}
		});

		fw.close();

	}

}
