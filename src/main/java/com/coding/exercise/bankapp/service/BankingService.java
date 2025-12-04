package com.coding.exercise.bankapp.service;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service API for the banking domain providing CRUD for customers/accounts and monetary operations.
 * Mirrors the original application's behaviors and DTO/entity mappings.
 */
// PUBLIC_INTERFACE
public interface BankingService {

    /**
     * Create a new customer entry.
     *
     * @param customerDetails domain DTO with nested address and contact
     * @return saved customer details with generated id
     */
    // PUBLIC_INTERFACE
    CustomerDetails createCustomer(CustomerDetails customerDetails);

    /**
     * Retrieve a customer by id.
     *
     * @param customerId primary key
     * @return optional customer details
     */
    // PUBLIC_INTERFACE
    Optional<CustomerDetails> getCustomer(Long customerId);

    /**
     * Retrieve all customers.
     *
     * @return list of customers
     */
    // PUBLIC_INTERFACE
    List<CustomerDetails> getCustomers();

    /**
     * Update an existing customer entry. Missing nested objects are left as-is if null, else replaced.
     *
     * @param customerId id of the customer
     * @param customerDetails data to update
     * @return updated customer details
     * @throws IllegalArgumentException if customer not found
     */
    // PUBLIC_INTERFACE
    CustomerDetails updateCustomer(Long customerId, CustomerDetails customerDetails);

    /**
     * Delete a customer by id.
     *
     * @param customerId id to delete
     */
    // PUBLIC_INTERFACE
    void deleteCustomer(Long customerId);

    /**
     * Create a bank account and optionally associate to an existing customer.
     *
     * @param customerId optional customer id to link (can be null to create unlinked account)
     * @param accountInformation domain DTO for account
     * @return saved account information with id
     */
    // PUBLIC_INTERFACE
    AccountInformation createAccount(Long customerId, AccountInformation accountInformation);

    /**
     * Retrieve a single account by id.
     *
     * @param accountId id
     * @return optional account info
     */
    // PUBLIC_INTERFACE
    Optional<AccountInformation> getAccount(Long accountId);

    /**
     * Retrieve all accounts.
     *
     * @return list of accounts
     */
    // PUBLIC_INTERFACE
    List<AccountInformation> getAccounts();

    /**
     * Update account metadata (not balance).
     *
     * @param accountId id to update
     * @param accountInformation new data
     * @return updated account info
     * @throws IllegalArgumentException if account not found
     */
    // PUBLIC_INTERFACE
    AccountInformation updateAccount(Long accountId, AccountInformation accountInformation);

    /**
     * Delete an account by id.
     *
     * @param accountId id
     */
    // PUBLIC_INTERFACE
    void deleteAccount(Long accountId);

    /**
     * Perform a deposit into an account.
     *
     * @param accountId id of target account
     * @param amount amount to deposit (must be > 0)
     * @return resulting balance
     */
    // PUBLIC_INTERFACE
    BigDecimal deposit(Long accountId, BigDecimal amount);

    /**
     * Perform a withdrawal from an account.
     *
     * @param accountId id of source account
     * @param amount amount to withdraw (must be > 0 and <= current balance)
     * @return resulting balance
     * @throws IllegalArgumentException when insufficient funds
     */
    // PUBLIC_INTERFACE
    BigDecimal withdraw(Long accountId, BigDecimal amount);

    /**
     * Transfer funds between accounts atomically.
     *
     * @param fromAccountId source account
     * @param toAccountId destination account
     * @param amount amount to transfer (must be > 0 and <= source balance)
     * @return resulting source balance
     * @throws IllegalArgumentException when insufficient funds or invalid accounts
     */
    // PUBLIC_INTERFACE
    BigDecimal transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);

    /**
     * Get transaction history for an account.
     *
     * @param accountId id
     * @return list of transactions associated to the account
     */
    // PUBLIC_INTERFACE
    List<TransactionDetails> getTransactions(Long accountId);

    /**
     * Helper: Return current balance of an account.
     *
     * @param accountId id
     * @return current balance
     */
    // PUBLIC_INTERFACE
    BigDecimal getBalance(Long accountId);

    /**
     * Helper: Return balances for all accounts for a customer.
     *
     * @param customerId id
     * @return list of account balances for that customer
     */
    // PUBLIC_INTERFACE
    List<BigDecimal> getBalancesForCustomer(Long customerId);
}
