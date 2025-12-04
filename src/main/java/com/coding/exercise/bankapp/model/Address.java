package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Address value entity associated to Customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 128)
    private String line1;

    @Size(max = 128)
    private String line2;

    @NotBlank
    @Size(max = 64)
    private String city;

    @NotBlank
    @Size(max = 64)
    private String state;

    @NotBlank
    @Size(max = 16)
    private String zip;

    @NotBlank
    @Size(max = 64)
    private String country;
}
