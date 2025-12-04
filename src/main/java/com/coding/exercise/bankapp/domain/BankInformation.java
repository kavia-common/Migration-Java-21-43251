package com.coding.exercise.bankapp.domain;

import lombok.*;

/**
 * DTO for BankInfo data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankInformation {
    private String bankCode;
    private String bankName;
}
