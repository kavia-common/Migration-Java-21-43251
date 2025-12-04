package com.coding.exercise.bankapp.domain;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representing a transfer request between accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDetails {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
}
