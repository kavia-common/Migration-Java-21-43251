package com.coding.exercise.bankapp.controller;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * AccountController exposes REST endpoints for account lifecycle and monetary operations.
 * All routes are served under the global context-path (/bank-api).
 */
@Tag(name = "Accounts", description = "Account CRUD, balances, and monetary operations")
@RestController
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    private final BankingService bankingService;

    public AccountController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    /**
     * PUBLIC_INTERFACE
     * Create a new account. Optionally link to a customer via query parameter customerId.
     *
     * @param customerId optional customer id to link
     * @param request    account info
     * @return created account info
     */
    // PUBLIC_INTERFACE
    @Operation(
            summary = "Create account",
            description = "Creates a new account and optionally associates it to a given customer.",
            responses = @ApiResponse(responseCode = "200", description = "Created",
                    content = @Content(schema = @Schema(implementation = AccountInformation.class)))
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountInformation> createAccount(
            @Parameter(description = "Optional customer id to link") @RequestParam(name = "customerId", required = false) Long customerId,
            @Valid @RequestBody AccountInformation request) {
        AccountInformation created = bankingService.createAccount(customerId, request);
        return ResponseEntity.ok(created);
    }

    /**
     * PUBLIC_INTERFACE
     * Get a list of all accounts.
     *
     * @return list of accounts
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "List accounts", description = "Retrieves all accounts")
    @GetMapping
    public ResponseEntity<List<AccountInformation>> getAccounts() {
        return ResponseEntity.ok(bankingService.getAccounts());
    }

    /**
     * PUBLIC_INTERFACE
     * Retrieve a specific account by id.
     *
     * @param accountId account id
     * @return account info or 404
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Get account", description = "Retrieve a single account by id")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountInformation> getAccount(@PathVariable Long accountId) {
        Optional<AccountInformation> info = bankingService.getAccount(accountId);
        return info.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUBLIC_INTERFACE
     * Update account metadata (not balance).
     *
     * @param accountId id
     * @param request   updated info
     * @return updated account info
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Update account", description = "Updates account metadata (type, currency, bank info)")
    @PutMapping(value = "/{accountId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountInformation> updateAccount(@PathVariable Long accountId,
                                                            @Valid @RequestBody AccountInformation request) {
        AccountInformation updated = bankingService.updateAccount(accountId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUBLIC_INTERFACE
     * Delete an account.
     *
     * @param accountId id
     * @return 204
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Delete account", description = "Deletes the account by id")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        bankingService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUBLIC_INTERFACE
     * Get current balance for an account.
     *
     * @param accountId id
     * @return balance
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Get balance", description = "Returns current account balance")
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(bankingService.getBalance(accountId));
    }

    /**
     * PUBLIC_INTERFACE
     * Deposit into an account.
     *
     * @param accountId id
     * @param amount    positive amount
     * @return resulting balance
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Deposit", description = "Deposits an amount into the account")
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<BigDecimal> deposit(@PathVariable Long accountId,
                                              @RequestParam @Positive BigDecimal amount) {
        return ResponseEntity.ok(bankingService.deposit(accountId, amount));
    }

    /**
     * PUBLIC_INTERFACE
     * Withdraw from an account.
     *
     * @param accountId id
     * @param amount    positive amount
     * @return resulting balance
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Withdraw", description = "Withdraws an amount from the account")
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<BigDecimal> withdraw(@PathVariable Long accountId,
                                               @RequestParam @Positive BigDecimal amount) {
        return ResponseEntity.ok(bankingService.withdraw(accountId, amount));
    }

    /**
     * PUBLIC_INTERFACE
     * Transfer funds from one account to another.
     *
     * @param fromAccountId source id
     * @param toAccountId   destination id
     * @param amount        positive amount
     * @return resulting source balance
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Transfer", description = "Transfers funds from source account to destination account")
    @PostMapping("/transfer")
    public ResponseEntity<BigDecimal> transfer(@RequestParam Long fromAccountId,
                                               @RequestParam Long toAccountId,
                                               @RequestParam @Positive BigDecimal amount) {
        return ResponseEntity.ok(bankingService.transfer(fromAccountId, toAccountId, amount));
    }

    /**
     * PUBLIC_INTERFACE
     * Get transaction history for an account.
     *
     * @param accountId id
     * @return list of transaction details
     */
    // PUBLIC_INTERFACE
    @Operation(summary = "Transactions", description = "Retrieves transaction history for an account")
    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionDetails>> getTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(bankingService.getTransactions(accountId));
    }
}
