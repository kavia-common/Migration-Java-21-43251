package com.coding.exercise.bankapp.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO representing a transaction entry on an account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails {
    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    private String description;
    private OffsetDateTime createdAt;
}
