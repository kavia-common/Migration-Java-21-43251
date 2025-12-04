package com.coding.exercise.bankapp.repository;

import com.coding.exercise.bankapp.model.CustomerAccountXRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for CustomerAccountXRef entity enabling lookups of account links per customer and vice versa.
 */
// PUBLIC_INTERFACE
public interface CustomerAccountXRefRepository extends JpaRepository<CustomerAccountXRef, Long> {

    /**
     * Find all mappings for a given customer id.
     *
     * @param customerId customer primary key
     * @return list of mappings
     */
    // PUBLIC_INTERFACE
    List<CustomerAccountXRef> findByCustomer_Id(Long customerId);

    /**
     * Find all mappings for a given account id.
     *
     * @param accountId account primary key
     * @return list of mappings
     */
    // PUBLIC_INTERFACE
    List<CustomerAccountXRef> findByAccount_Id(Long accountId);
}
