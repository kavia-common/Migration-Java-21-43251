package com.coding.exercise.bankapp.domain;

import lombok.*;

/**
 * DTO carrying account metadata for responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInformation {
    private Long id;
    private String accountNumber;
    private String type;
    private String currency;
    private String bankCode;
    private String bankName;
}
