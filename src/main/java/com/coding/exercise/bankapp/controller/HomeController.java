package com.coding.exercise.bankapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HomeController provides a simple landing endpoint to verify the API is reachable
 * under the configured context-path (/bank-api). This mirrors the presence of a
 * "home" endpoint in the source application for easy smoke checks.
 */
@Tag(name = "Home", description = "Landing and simple info endpoints")
@RestController
public class HomeController {

    /**
     * PUBLIC_INTERFACE
     * Returns basic information about the API.
     *
     * @return a map with a brief message and links hint
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "API Home", description = "Basic info to confirm the Bank API is running.")
    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> home() {
        return Map.of(
                "message", "BankApp Java21 API is running",
                "docs", "/bank-api/swagger-ui",
                "health", "/bank-api/health",
                "actuatorHealth", "/bank-api/actuator/health"
        );
    }
}
