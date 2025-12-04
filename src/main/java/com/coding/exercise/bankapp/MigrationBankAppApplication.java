package com.coding.exercise.bankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MigrationBankAppApplication is the entry point for the Java 21 Spring Boot application
 * used during the migration scaffold for BankApp-Java21.
 *
 * The application is configured to:
 * - Run on a configurable port (default 3002) and address 0.0.0.0
 * - Use a servlet context path of /bank-api
 * - Expose Spring Boot Actuator health and info endpoints
 *
 * Note: This scaffold intentionally contains only the baseline bootstrapping;
 * controllers and domain logic will be added in later steps of the migration.
 */
// PUBLIC_INTERFACE
@SpringBootApplication
public class MigrationBankAppApplication {

    /**
     * Application entry point.
     *
     * @param args standard program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MigrationBankAppApplication.class, args);
    }
}
