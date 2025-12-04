package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a bank account.
 * This mirrors the original BankApp model structure and field names for parity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "account_number", nullable = false, unique = true, length = 64)
    private String accountNumber;

    @NotNull
    @Column(name = "type", nullable = false, length = 32)
    private String type; // e.g., CHECKING, SAVINGS

    @NotNull
    @PositiveOrZero
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // ISO 4217 (e.g., USD)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_info_id")
    private BankInfo bankInfo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
