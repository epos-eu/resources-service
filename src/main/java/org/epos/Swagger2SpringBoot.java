package org.epos;


import org.epos.api.core.EnvironmentVariables;
import org.epos.api.core.ZabbixExecutor;
import org.epos.api.facets.Facets;
import org.epos.configuration.LocalDateConverter;
import org.epos.configuration.LocalDateTimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.oas.annotations.EnableOpenApi;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableOpenApi
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = { "org.epos", "org.epos.api", "org.epos.api.routines", "org.epos.configuration"})
public class Swagger2SpringBoot implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(Swagger2SpringBoot.class);
	
	@Override
	public void run(String... arg0) throws Exception {
		if (arg0.length > 0 && arg0[0].equals("exitcode")) {
			throw new ExitException();
		}
		LOGGER.info("[Facets enabled]"); 
		Facets.getInstance();
		if(EnvironmentVariables.MONITORING.equals("true")) LOGGER.info("[Monitoring enabled]");
		else System.out.println("[Monitoring disabled]");
		if(EnvironmentVariables.MONITORING.equals("true")) {
			ZabbixExecutor.getInstance();
		}
	}

	public static void main(String[] args) throws Exception {
		new SpringApplication(Swagger2SpringBoot.class).run(args);
	}

	@Configuration
	static class CustomDateConfig extends WebMvcConfigurerAdapter {
		@Override
		public void addFormatters(FormatterRegistry registry) {
			registry.addConverter(new LocalDateConverter("yyyy-MM-dd"));
			registry.addConverter(new LocalDateTimeConverter("yyyy-MM-dd'T'HH:mm:ss.SSS"));
		}
	}

	class ExitException extends RuntimeException implements ExitCodeGenerator {
		private static final long serialVersionUID = 1L;

		@Override
		public int getExitCode() {
			return 10;
		}

	}
}
