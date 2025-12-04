package com.coding.exercise.bankapp.config;

import com.coding.exercise.bankapp.model.*;
import com.coding.exercise.bankapp.repository.AccountRepository;
import com.coding.exercise.bankapp.repository.CustomerAccountXRefRepository;
import com.coding.exercise.bankapp.repository.CustomerRepository;
import com.coding.exercise.bankapp.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * DataInitializer seeds sample customers, accounts, account-customer links (xrefs),
 * and initial transactions on application startup.
 *
 * Profile: runs only when the "seed" profile is active to avoid unintentional seeding.
 *
 * Idempotence:
 * - Customers are located by (firstName, lastName, dob) and created if absent.
 * - Accounts are located by unique accountNumber and created if absent.
 * - XRefs are checked for existing link (customerId, accountId) before creating.
 * - Transactions are seeded only when the target account has no existing transactions.
 *
 * Note on BankInfo:
 * - To align with existing repositories and avoid relying on cascades that do not exist
 *   on Account.bankInfo (ManyToOne without cascade), this seeder does not persist BankInfo.
 *   Accounts are created with null BankInfo; other application flows may populate it.
 */
// PUBLIC_INTERFACE
@Component
@Profile({"seed"})
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CustomerAccountXRefRepository xrefRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           CustomerAccountXRefRepository xrefRepository,
                           TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.xrefRepository = xrefRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * PUBLIC_INTERFACE
     * Seeds initial data if not present:
     * - Two customers (Alice Anderson and Bob Baker) with address/contact
     * - Two accounts (one per customer) with initial balances (USD)
     * - Links each customer to their account (PRIMARY role)
     * - Creates an initial deposit transaction per account if none exist
     */
    // PUBLIC_INTERFACE
    @Override
    @Transactional
    public void run(String... args) {
        log.info("DataInitializer starting (profile=seed)");

        // Seed customers
        Customer alice = ensureCustomer(
                "Alice", "Anderson", LocalDate.of(1990, 1, 10),
                "123 Main St", null, "Springfield", "CA", "90210", "USA",
                "555-111-2222", "alice@example.com"
        );

        Customer bob = ensureCustomer(
                "Bob", "Baker", LocalDate.of(1985, 5, 23),
                "987 Oak Ave", "Apt 2", "Riverside", "TX", "77001", "USA",
                "555-333-4444", "bob@example.com"
        );

        // Seed accounts (no BankInfo to avoid unsaved transient entity issues)
        Account aliceAcct = ensureAccount(
                "CHK-10001", "CHECKING", "USD", new BigDecimal("1500.00")
        );
        Account bobAcct = ensureAccount(
                "SAV-20001", "SAVINGS", "USD", new BigDecimal("2500.00")
        );

        // Associate xrefs (PRIMARY role)
        ensureXref(alice, aliceAcct, "PRIMARY");
        ensureXref(bob, bobAcct, "PRIMARY");

        // Initial transactions if none recorded yet (simple presence check)
        ensureInitialDeposit(aliceAcct, new BigDecimal("1500.00"), "Initial deposit for Alice");
        ensureInitialDeposit(bobAcct, new BigDecimal("2500.00"), "Initial deposit for Bob");

        log.info("DataInitializer completed");
    }

    private Customer ensureCustomer(String firstName,
                                    String lastName,
                                    LocalDate dob,
                                    String line1, String line2, String city, String state, String zip, String country,
                                    String phone, String email) {

        Optional<Customer> existing = customerRepository.findAll().stream()
                .filter(c -> c.getFirstName().equals(firstName)
                        && c.getLastName().equals(lastName)
                        && ((c.getDateOfBirth() == null && dob == null) || (c.getDateOfBirth() != null && c.getDateOfBirth().equals(dob))))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        Address address = Address.builder()
                .line1(line1)
                .line2(line2)
                .city(city)
                .state(state)
                .zip(zip)
                .country(country)
                .build();

        Contact contact = Contact.builder()
                .phone(phone)
                .email(email)
                .build();

        Customer customer = Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dob)
                .address(address)
                .contact(contact)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Created customer {} {} (id={})", firstName, lastName, saved.getId());
        return saved;
    }

    private Account ensureAccount(String accountNumber,
                                  String type,
                                  String currency,
                                  BigDecimal initialBalance) {

        // Check by unique account number
        Optional<Account> existing = accountRepository.findByAccountNumber(accountNumber);
        if (existing.isPresent()) {
            return existing.get();
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .type(type)
                .currency(currency)
                .balance(initialBalance.setScale(2, java.math.RoundingMode.HALF_UP))
                .createdAt(OffsetDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Created account {} (id={}) with balance {}", accountNumber, saved.getId(), saved.getBalance());
        return saved;
    }

    private void ensureXref(Customer customer, Account account, String role) {
        boolean exists = xrefRepository.findByCustomer_Id(customer.getId())
                .stream()
                .anyMatch(x -> x.getAccount() != null && x.getAccount().getId().equals(account.getId()));
        if (exists) return;

        CustomerAccountXRef xref = CustomerAccountXRef.builder()
                .customer(customer)
                .account(account)
                .role(role)
                .build();
        xrefRepository.save(xref);
        log.info("Linked customer {} to account {} as {}", customer.getId(), account.getId(), role);
    }

    private void ensureInitialDeposit(Account account, BigDecimal amount, String description) {
        // If any transaction exists for the account, assume it's already initialized
        boolean hasAnyTxn = !transactionRepository.findByAccount_Id(account.getId()).isEmpty();
        if (hasAnyTxn) return;

        BigDecimal scaledAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP);

        // Ensure account balance reflects intended initial balance
        if (account.getBalance() == null || account.getBalance().compareTo(scaledAmount) != 0) {
            account.setBalance(scaledAmount);
            account.setUpdatedAt(OffsetDateTime.now());
            accountRepository.save(account);
        }

        Transaction txn = Transaction.builder()
                .account(account)
                .amount(scaledAmount)
                .type("DEPOSIT")
                .description(description)
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(txn);

        log.info("Seeded initial deposit {} {} for account {}", account.getCurrency(), scaledAmount, account.getAccountNumber());
    }
}
