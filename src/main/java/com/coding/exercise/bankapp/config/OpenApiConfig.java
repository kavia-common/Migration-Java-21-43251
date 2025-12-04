package com.coding.exercise.bankapp.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

/**
 * OpenApiConfig customizes the generated OpenAPI definition.
 *
 * The customizer ensures the "servers" list contains a single relative entry that matches
 * the servlet context-path (e.g., "/bank-api"). Using a relative server URL avoids mixed
 * content and scheme mismatches in environments where HTTPS is terminated by a reverse proxy.
 *
 * Additionally, a ForwardedHeaderFilter bean is provided to let Spring respect X-Forwarded-*
 * headers and reconstruct the correct scheme/host/port, in conjunction with the
 * "server.forward-headers-strategy=framework" setting in application.yml.
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
     * Customizes the OpenAPI definition to set a single relative server URL equal to the current
     * servlet context path. With a relative URL (e.g., "/bank-api"), Swagger UI will inherit the
     * correct scheme (https) and host from the page it is served from, preventing mixed-content/CORS issues.
     *
     * @param contextPath current servlet context-path (e.g., "/bank-api"), may be empty
     * @return the OpenApiCustomiser that applies the relative server URL
     */
    // PUBLIC_INTERFACE
    @Bean
    public OpenApiCustomiser serverOpenApiCustomiser(
            @Value("${server.servlet.context-path:}") String contextPath) {
        return openApi -> {
            String basePath = (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            if (basePath.length() > 1 && basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            openApi.setServers(List.of(new Server().url(basePath)));
        };
    }
}
