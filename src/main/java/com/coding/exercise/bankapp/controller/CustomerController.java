package com.coding.exercise.bankapp.controller;

import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CustomerController exposes CRUD operations for customers.
 */
@Tag(name = "Customers", description = "Customer CRUD and helpers")
@RestController
@RequestMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

    private final BankingService bankingService;

    public CustomerController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    /**
     * PUBLIC_INTERFACE
     * Create a new customer.
     *
     * @param request customer details
     * @return created customer details
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Create customer", description = "Creates a new customer")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDetails> createCustomer(@Valid @RequestBody CustomerDetails request) {
        return ResponseEntity.ok(bankingService.createCustomer(request));
    }

    /**
     * PUBLIC_INTERFACE
     * Get all customers.
     *
     * @return list of customer details
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "List customers", description = "Retrieves all customers")
    @GetMapping
    public ResponseEntity<List<CustomerDetails>> getCustomers() {
        return ResponseEntity.ok(bankingService.getCustomers());
    }

    /**
     * PUBLIC_INTERFACE
     * Get a single customer by id.
     *
     * @param customerId id
     * @return details or 404
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Get customer", description = "Retrieve a customer by id")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDetails> getCustomer(@PathVariable Long customerId) {
        Optional<CustomerDetails> details = bankingService.getCustomer(customerId);
        return details.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUBLIC_INTERFACE
     * Update a customer by id. Missing nested objects are left as-is if null in the request.
     *
     * @param customerId id
     * @param request    updates
     * @return updated details
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Update customer", description = "Updates customer information")
    @PutMapping(value = "/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDetails> updateCustomer(@PathVariable Long customerId,
                                                          @Valid @RequestBody CustomerDetails request) {
        return ResponseEntity.ok(bankingService.updateCustomer(customerId, request));
    }

    /**
     * PUBLIC_INTERFACE
     * Delete a customer.
     *
     * @param customerId id
     * @return 204
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Delete customer", description = "Deletes a customer by id")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        bankingService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUBLIC_INTERFACE
     * Get balances for all accounts linked to a customer.
     *
     * @param customerId id
     * @return list of balances
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Customer balances", description = "Returns balances for all accounts linked to the customer")
    @GetMapping("/{customerId}/balances")
    public ResponseEntity<List<BigDecimal>> getBalancesForCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(bankingService.getBalancesForCustomer(customerId));
    }
}
