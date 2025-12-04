package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Transaction entity recording movements on accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_account_id", columnList = "account_id"),
        @Index(name = "idx_txn_created_at", columnList = "created_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "type", nullable = false, length = 16)
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
