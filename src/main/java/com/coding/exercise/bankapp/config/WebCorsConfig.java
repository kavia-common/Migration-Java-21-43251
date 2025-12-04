package com.coding.exercise.bankapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * WebCorsConfig sets up global CORS configuration for the Spring Boot 3 application.
 * It allows configured origins to perform cross-origin requests to all endpoints under the
 * servlet context-path (/bank-api). This is required for Swagger UI (and any browser UI)
 * to call endpoints like /bank-api/health without CORS failures.
 *
 * Allowed origins are read from the ALLOWED_ORIGINS environment variable if present. If not,
 * a secure default set of origins is applied (preview domains and common localhost ports).
 */
// PUBLIC_INTERFACE
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private static final String DEFAULT_ALLOWED_ORIGINS =
            "https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3000," +
            "https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002," +
            "http://localhost:3000," +
            "http://localhost:4000";

    private final String[] allowedOrigins;

    /**
     * Constructs the CORS config with origins coming from ALLOWED_ORIGINS or a secure default.
     *
     * @param allowedOriginsProp comma-separated origins from env/property ALLOWED_ORIGINS
     */
    public WebCorsConfig(
            @Value("${ALLOWED_ORIGINS:" + DEFAULT_ALLOWED_ORIGINS + "}") String allowedOriginsProp) {
        this.allowedOrigins = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Configure CORS mappings to apply to all routes under the context-path.
     * Allows common REST methods, required headers, exposes selected headers, and enables credentials.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(this.allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Origin", "Accept")
                .exposedHeaders("Location", "Link")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
