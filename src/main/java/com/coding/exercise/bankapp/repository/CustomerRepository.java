package com.coding.exercise.bankapp.repository;

import com.coding.exercise.bankapp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Customer entity with common finder methods.
 */
// PUBLIC_INTERFACE
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customers by last name.
     *
     * @param lastName last name
     * @return list of customers
     */
    // PUBLIC_INTERFACE
    List<Customer> findByLastName(String lastName);

    /**
     * Find customers by first name.
     *
     * @param firstName first name
     * @return list of customers
     */
    // PUBLIC_INTERFACE
    List<Customer> findByFirstName(String firstName);
}
