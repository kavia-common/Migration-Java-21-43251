package com.coding.exercise.bankapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * SecurityConfig configures Spring Security for the Java 21 application.
 *
 * Current behavior (mirrors source application state for development):
 * - All requests are permitted (no authentication).
 * - CSRF protection is disabled.
 * - Frame-Options is set to sameOrigin so the H2 console can be embedded.
 * - Static assets and Swagger/OpenAPI endpoints are ignored by the security filter chain.
 *
 * Notes for production:
 * - Replace permitAll with least privilege rules and enable CSRF where applicable.
 * - Restrict frame options or disable H2 console entirely in production.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PUBLIC_INTERFACE
     * Defines the primary HTTP security filter chain.
     *
     * - Disables CSRF for simplicity in development environments.
     * - Sets frameOptions to sameOrigin to allow embedded H2 console rendering.
     * - Permits all requests (no authentication).
     * - Disables basic and form login.
     *
     * @param http HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    // PUBLIC_INTERFACE
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Allow H2 console frames (same origin)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            // Disable CSRF for dev convenience
            .csrf(AbstractHttpConfigurer::disable)
            // Permit all requests (parity with current source app)
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            // Disable built-in login mechanisms
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * PUBLIC_INTERFACE
     * Instructs Spring Security to ignore requests for static resources and Swagger/OpenAPI documentation.
     * These paths bypass the security filter chain for improved performance and to ensure UI/docs work seamlessly.
     *
     * Ignored paths include:
     * - Common static assets under /css, /js, /images, /webjars
     * - Swagger UI and OpenAPI endpoints: /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html
     * - H2 console: /h2-console/**
     */
    // PUBLIC_INTERFACE
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                // Static assets
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**",
                // OpenAPI/Swagger endpoints
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                // H2 console
                "/h2-console/**"
        );
    }
}
