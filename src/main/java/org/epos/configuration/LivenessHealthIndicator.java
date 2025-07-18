package org.epos.configuration;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LivenessHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		int errorCode = check();
		if (errorCode != 0) {
			return Health.down().withDetail("No Database Connection", errorCode).build();
		}

		return Health.up().build();
	}

	private int check() {

//	TODO:	try {
//			EntityManager em = new DBService().getEntityManager();
//			em.createNativeQuery("select * from class_mapping cm").getResultList();
//		} catch (Exception ignored){
//			return 1;
//		}
		return 0;

	}
}
