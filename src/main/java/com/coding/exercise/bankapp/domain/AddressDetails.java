package com.coding.exercise.bankapp.domain;

import lombok.*;

/**
 * DTO for Address data used in requests/responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetails {
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zip;
    private String country;
}
