package com.coding.exercise.bankapp.repository;

import com.coding.exercise.bankapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Account entity providing CRUD and finder operations.
 */
// PUBLIC_INTERFACE
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find an account by its unique account number.
     *
     * @param accountNumber account number string
     * @return optional account
     */
    // PUBLIC_INTERFACE
    Optional<Account> findByAccountNumber(String accountNumber);
}
