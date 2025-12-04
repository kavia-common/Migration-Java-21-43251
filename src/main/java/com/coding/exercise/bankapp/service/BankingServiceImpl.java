package com.coding.exercise.bankapp.service;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.model.*;
import com.coding.exercise.bankapp.repository.AccountRepository;
import com.coding.exercise.bankapp.repository.CustomerAccountXRefRepository;
import com.coding.exercise.bankapp.repository.CustomerRepository;
import com.coding.exercise.bankapp.repository.TransactionRepository;
import com.coding.exercise.bankapp.repository.BankInfoRepository;
import com.coding.exercise.bankapp.service.helper.BankingServiceHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.coding.exercise.bankapp.service.helper.BankingServiceHelper.*;

/**
 * Implementation of BankingService providing CRUD and monetary operations.
 * Ensures parity with the original application (BankApp-179898).
 */
@Service
@RequiredArgsConstructor
public class BankingServiceImpl implements BankingService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAccountXRefRepository xRefRepository;
    private final TransactionRepository transactionRepository;
    private final BankInfoRepository bankInfoRepository;

    // region Customers CRUD

    @Override
    @Transactional
    public CustomerDetails createCustomer(CustomerDetails customerDetails) {
        Customer entity = toCustomerEntity(customerDetails);
        Customer saved = customerRepository.save(entity);
        return toCustomerDetails(saved);
    }

    @Override
    public Optional<CustomerDetails> getCustomer(Long customerId) {
        return customerRepository.findById(customerId).map(BankingServiceHelper::toCustomerDetails);
    }

    @Override
    public List<CustomerDetails> getCustomers() {
        return customerRepository.findAll().stream()
                .map(BankingServiceHelper::toCustomerDetails)
                .toList();
    }

    @Override
    @Transactional
    public CustomerDetails updateCustomer(Long customerId, CustomerDetails customerDetails) {
        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        applyNonNullCustomerUpdates(existing, customerDetails);
        Customer saved = customerRepository.save(existing);
        return toCustomerDetails(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) return;
        customerRepository.deleteById(customerId);
        // Note: depending on domain rules, you could also delete xrefs, but keep parity with source behavior.
    }

    // endregion

    // region Accounts CRUD

    @Override
    @Transactional
    public AccountInformation createAccount(Long customerId, AccountInformation accountInformation) {
        // Resolve/persist BankInfo only if bankCode is present; avoid invalid save with null/blank bankCode.
        BankInfo bankInfo = null;
        if (accountInformation != null) {
            String bankCode = accountInformation.getBankCode();
            String bankName = accountInformation.getBankName();
            if (bankCode != null && !bankCode.isBlank()) {
                bankInfo = bankInfoRepository.findByBankCode(bankCode)
                        .orElseGet(() -> bankInfoRepository.save(BankInfo.builder()
                                .bankCode(bankCode)
                                .bankName(bankName)
                                .build()));
            }
        }

        Account account = toAccountEntity(accountInformation, bankInfo);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        account.setBalance(scaled(account.getBalance()));
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(OffsetDateTime.now());
        }
        Account saved = accountRepository.save(account);

        // Optionally link to a customer
        if (customerId != null) {
            customerRepository.findById(customerId).ifPresent(customer -> {
                CustomerAccountXRef link = CustomerAccountXRef.builder()
                        .customer(customer)
                        .account(saved)
                        .role("PRIMARY")
                        .build();
                xRefRepository.save(link);
            });
        }

        return toAccountInformation(saved);
    }

    @Override
    public Optional<AccountInformation> getAccount(Long accountId) {
        return accountRepository.findById(accountId).map(BankingServiceHelper::toAccountInformation);
    }

    @Override
    public List<AccountInformation> getAccounts() {
        return accountRepository.findAll().stream()
                .map(BankingServiceHelper::toAccountInformation)
                .toList();
    }

    @Override
    @Transactional
    public AccountInformation updateAccount(Long accountId, AccountInformation accountInformation) {
        Account existing = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Resolve/persist BankInfo if bankCode provided; skip if only bankName present (can't persist without code).
        BankInfo bankInfo = null;
        if (accountInformation != null) {
            String bankCode = accountInformation.getBankCode();
            String bankName = accountInformation.getBankName();
            if (bankCode != null && !bankCode.isBlank()) {
                bankInfo = bankInfoRepository.findByBankCode(bankCode)
                        .orElseGet(() -> bankInfoRepository.save(BankInfo.builder()
                                .bankCode(bankCode)
                                .bankName(bankName)
                                .build()));
            }
        }

        applyNonNullAccountUpdates(existing, accountInformation, bankInfo);
        Account saved = accountRepository.save(existing);
        return toAccountInformation(saved);
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) return;
        accountRepository.deleteById(accountId);
        // As above, xref/tx cleanup follows source behavior (not enforced here).
    }

    // endregion

    // region Monetary operations

    @Override
    @Transactional
    public BigDecimal deposit(Long accountId, BigDecimal amount) {
        requirePositive(amount, "Deposit amount must be positive");
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        BigDecimal amt = scaled(amount);
        account.setBalance(scaled(account.getBalance().add(amt)));
        account.setUpdatedAt(OffsetDateTime.now());
        Account saved = accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .account(saved)
                .amount(amt)
                .type("DEPOSIT")
                .description("Deposit")
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(txn);

        return saved.getBalance();
    }

    @Override
    @Transactional
    public BigDecimal withdraw(Long accountId, BigDecimal amount) {
        requirePositive(amount, "Withdrawal amount must be positive");
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        BigDecimal amt = scaled(amount);
        if (account.getBalance().compareTo(amt) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(scaled(account.getBalance().subtract(amt)));
        account.setUpdatedAt(OffsetDateTime.now());
        Account saved = accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .account(saved)
                .amount(amt)
                .type("WITHDRAWAL")
                .description("Withdrawal")
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(txn);

        return saved.getBalance();
    }

    @Override
    @Transactional
    public BigDecimal transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null || fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Invalid accounts for transfer");
        }
        requirePositive(amount, "Transfer amount must be positive");
        BigDecimal amt = scaled(amount);

        Account from = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));
        Account to = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + toAccountId));

        // Validation: same currency constraint (parity with typical source behavior)
        if (from.getCurrency() != null && to.getCurrency() != null && !from.getCurrency().equals(to.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch for transfer");
        }

        if (from.getBalance().compareTo(amt) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Perform atomic transfer (single transaction)
        from.setBalance(scaled(from.getBalance().subtract(amt)));
        from.setUpdatedAt(OffsetDateTime.now());
        to.setBalance(scaled(to.getBalance().add(amt)));
        to.setUpdatedAt(OffsetDateTime.now());

        accountRepository.save(from);
        accountRepository.save(to);

        // Transactions: record both OUT and IN for transparency
        Transaction out = Transaction.builder()
                .account(from)
                .amount(amt)
                .type("TRANSFER_OUT")
                .description("Transfer to account " + to.getAccountNumber())
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(out);

        Transaction in = Transaction.builder()
                .account(to)
                .amount(amt)
                .type("TRANSFER_IN")
                .description("Transfer from account " + from.getAccountNumber())
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(in);

        return from.getBalance();
    }

    @Override
    public List<TransactionDetails> getTransactions(Long accountId) {
        // Ensure account exists for parity-like error reporting
        accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        return transactionRepository.findByAccount_Id(accountId).stream()
                .map(BankingServiceHelper::toTransactionDetails)
                .toList();
    }

    // endregion

    // region Helpers

    @Override
    public BigDecimal getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        return scaled(account.getBalance());
    }

    @Override
    public List<BigDecimal> getBalancesForCustomer(Long customerId) {
        // Find all account links for this customer and return balances (if account present).
        List<BigDecimal> balances = new ArrayList<>();
        xRefRepository.findByCustomer_Id(customerId).forEach(xref -> {
            Account acc = xref.getAccount();
            if (acc != null && acc.getBalance() != null) {
                balances.add(scaled(acc.getBalance()));
            }
        });
        return balances;
    }

    private static void requirePositive(BigDecimal amount, String message) {
        if (!isPositive(amount)) {
            throw new IllegalArgumentException(message);
        }
    }

    // endregion
}
