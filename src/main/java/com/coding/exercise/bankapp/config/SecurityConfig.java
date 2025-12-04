package com.coding.exercise.bankapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig configures Spring Security for the Java 21 application compatible with Spring Security 6.3.x.
 *
 * Behavior:
 * - Disables CSRF (development-friendly; not recommended for production without additional safeguards).
 * - Sets frame options to sameOrigin (to allow H2 console rendering in an iframe on the same origin).
 * - Permits all requests (including H2 console, Swagger/OpenAPI, Actuator health, and other endpoints).
 *
 * Notes:
 * - No WebSecurityCustomizer is defined (removed for Spring Security 6.3.x compatibility).
 * - For production, restrict access and enable appropriate protections as needed.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PUBLIC_INTERFACE
     * Defines the HTTP security filter chain using Spring Security 6.3.x APIs.
     *
     * - Disables CSRF.
     * - Sets headers.frameOptions().sameOrigin() to support H2 console frames.
     * - Permits all requests including:
     *   /h2-console/**, /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html, /actuator/health,
     *   and all other endpoints.
     *
     * @param http the HttpSecurity to configure
     * @return a built SecurityFilterChain
     * @throws Exception if configuration fails
     */
    // PUBLIC_INTERFACE
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Allow H2 console frames (same origin)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Disable CSRF for dev convenience
            .csrf(csrf -> csrf.disable())
            // Authorize all requests (maintains permissive dev behavior)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/h2-console/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
