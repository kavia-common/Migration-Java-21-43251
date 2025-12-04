package com.coding.exercise.bankapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;
import java.util.Optional;

/**
 * OpenApiConfig provides OpenAPI (Swagger) configuration for Springdoc v2.x on Spring Boot 3.3.
 *
 * Key points:
 * - Define an OpenAPI bean and set a single relative Server URL that matches the servlet context-path
 *   (e.g., "/bank-api"). A relative URL ensures Swagger UI inherits the correct scheme/host/port from the page,
 *   avoiding mixed content issues and enabling Try-it-Out to work with HTTPS and proxies.
 * - Provide basic app-level metadata (title, version, description) for the OpenAPI document.
 * - Optionally define a GroupedOpenApi bean to control grouping/selection of included paths.
 * - Register a ForwardedHeaderFilter to let the framework honor X-Forwarded-* headers (in combination with
 *   server.forward-headers-strategy=framework in application.yml).
 */
// PUBLIC_INTERFACE
@Configuration
public class OpenApiConfig {

    /**
     * Registers a ForwardedHeaderFilter so Spring MVC correctly interprets X-Forwarded-* headers
     * provided by upstream proxies/load balancers. This helps produce correct absolute URLs when required.
     *
     * Note: application.yml sets server.forward-headers-strategy=framework to enable framework support.
     *
     * @return a configured ForwardedHeaderFilter bean
     */
    // PUBLIC_INTERFACE
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    /**
     * Configure the OpenAPI definition and set the servers list to a single relative entry based on
     * the servlet context path. With a relative URL (e.g., "/bank-api"), Swagger UI will inherit the
     * correct scheme (https) and host from the page it is served from, preventing mixed-content/CORS issues.
     * Also sets basic document metadata (title, version, description).
     *
     * @param contextPath current servlet context-path (e.g., "/bank-api"), may be empty
     * @return the configured OpenAPI bean
     */
    // PUBLIC_INTERFACE
    @Bean
    public OpenAPI applicationOpenAPI(@Value("${server.servlet.context-path:}") String contextPath) {
        String basePath = (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        if (basePath.length() > 1 && basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        // Derive version from package metadata if available; fallback to "v1"
        String appVersion = Optional.ofNullable(OpenApiConfig.class.getPackage().getImplementationVersion())
                .orElse("v1");

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("BankApp Java21 API")
                        .version(appVersion)
                        .description("OpenAPI specification for BankApp (Java 21, Spring Boot 3.3.x)"))
                .servers(List.of(new Server().url(basePath)));

        return openAPI;
    }

    /**
     * Optional GroupedOpenApi to organize or filter endpoints included in the OpenAPI document.
     * By default, includes all paths. Adjust pathsToMatch/pathsToExclude as needed.
     *
     * @return GroupedOpenApi bean named "bankapp"
     */
    // PUBLIC_INTERFACE
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("bankapp")
                .pathsToMatch("/**")
                .build();
    }
}
