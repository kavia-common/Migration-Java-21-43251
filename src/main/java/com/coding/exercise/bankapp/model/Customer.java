package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Customer entity representing a bank customer with address and contact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 64)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 64)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id")
    private Contact contact;
}
