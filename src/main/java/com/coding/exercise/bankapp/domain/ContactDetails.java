package com.coding.exercise.bankapp.domain;

import lombok.*;

/**
 * DTO for Contact data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetails {
    private String phone;
    private String email;
}
