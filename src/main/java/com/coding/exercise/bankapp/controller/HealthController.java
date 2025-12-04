package com.coding.exercise.bankapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController exposes a simple health check endpoint at GET /health.
 * This is provided in addition to Spring Boot Actuator's health endpoint.
 * The application is configured with a context path of /bank-api, therefore
 * the full URL will be /bank-api/health.
 */
// PUBLIC_INTERFACE
@RestController
public class HealthController {

    /**
     * Returns a simple health status payload.
     *
     * @return a JSON map containing {"status":"UP"}
     */
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return response;
    }
}
