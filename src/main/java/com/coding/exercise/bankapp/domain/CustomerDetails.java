package com.coding.exercise.bankapp.domain;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO for Customer data including nested address and contact details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetails {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private AddressDetails address;
    private ContactDetails contact;
}
