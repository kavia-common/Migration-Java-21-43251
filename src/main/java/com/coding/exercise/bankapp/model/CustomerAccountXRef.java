package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Cross-reference entity to map Customers to Accounts (many-to-many via explicit entity).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "customer_account_xref",
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "account_id"}))
public class CustomerAccountXRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "role", length = 32)
    private String role; // e.g., PRIMARY, JOINT
}
