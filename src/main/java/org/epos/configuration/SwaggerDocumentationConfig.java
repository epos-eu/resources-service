package org.epos.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-11T14:51:06.469Z[GMT]")
@Configuration
public class SwaggerDocumentationConfig {
	
	 @Bean
	    public Docket customImplementation(){
	        return new Docket(DocumentationType.OAS_30)
		                .select()
	                    .apis(RequestHandlerSelectors.basePackage("org.epos.api"))
	                    .build()
	                .directModelSubstitute(org.threeten.bp.LocalDate.class, java.sql.Date.class)
	                .directModelSubstitute(org.threeten.bp.OffsetDateTime.class, java.util.Date.class)
	                .apiInfo(apiInfo());
	    }
	   
	    ApiInfo apiInfo() {
	        return new ApiInfoBuilder()
	            .title("Resources Service RESTful APIs")
	            .description("This is the Resources Service RESTful APIs Swagger page.")
	            .license("MIT License")
	            .licenseUrl("https://epos-ci.brgm.fr/epos/WebApi/raw/master/LICENSE")
	            .termsOfServiceUrl("")
	            .version(System.getenv("VERSION"))
	            .contact(new Contact("","", "apis@lists.epos-ip.org"))
	            .build();
	    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Resources Service RESTful APIs")
                .description("This is the Resources Service RESTful APIs Swagger page.")
                .termsOfService("")
                .version(System.getenv("VERSION"))
                .license(new License()
                    .name("MIT License")
                    .url("https://epos-ci.brgm.fr/epos/WebApi/raw/master/LICENSE"))
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .email("apis@lists.epos-ip.org")));
    }
    
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
            List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
            Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
            allEndpoints.addAll(webEndpoints);
            allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
            allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
            String basePath = webEndpointProperties.getBasePath();
            EndpointMapping endpointMapping = new EndpointMapping(basePath);
            boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
            return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping, null);
        }


    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
            return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
        }

}
