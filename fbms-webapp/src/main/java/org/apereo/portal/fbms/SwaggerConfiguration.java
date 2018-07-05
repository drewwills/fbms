package org.apereo.portal.fbms;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * Configuration for Swagger API documentation and HTML client.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    /**
     * This string is used to link the Swagger <code>ApiKey</code> to the <code>SecurityReference</code>.
     */
    private static final String API_KEY_NAME = "api_key";

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiKey apiKey() {
        return new ApiKey(API_KEY_NAME, HttpHeaders.AUTHORIZATION, In.HEADER.name());
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(securityReference()))
                .forPaths(PathSelectors.regex("/api.*"))
                .build();
    }

    private SecurityReference securityReference() {
        final AuthorizationScope authorizationScope =
                new AuthorizationScope("global", "Access REST APIs");
        return new SecurityReference(API_KEY_NAME, new AuthorizationScope[] { authorizationScope });
    }

}