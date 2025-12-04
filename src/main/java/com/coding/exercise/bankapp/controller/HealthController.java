package com.coding.exercise.bankapp.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController exposes simple health check endpoints at GET /health and GET /healthz.
 * These are provided in addition to Spring Boot Actuator's health endpoint.
 * The application is configured with a context path of /bank-api, therefore
 * the full URLs will be /bank-api/health and /bank-api/healthz.
 */
// PUBLIC_INTERFACE
@Tag(name = "Health", description = "Simple application health check endpoints")
@RestController
public class HealthController {

    /**
     * PUBLIC_INTERFACE
     * Returns a simple health status payload.
     *
     * @return a JSON map containing {"status":"UP"}
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Health check", description = "Returns a simple status payload indicating the application is UP")
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return response;
    }

    /**
     * PUBLIC_INTERFACE
     * Returns a healthz response for compatibility with the source application.
     *
     * @return a JSON map containing {"status":"ok"}
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Health check (healthz)", description = "Compatibility endpoint returning a simple ok status")
    @GetMapping(path = "/healthz", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> healthz() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }
}
