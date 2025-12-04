package com.coding.exercise.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * BankInfo holds bank metadata (e.g., routing number, name).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "bank_info")
public class BankInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 64)
    @Column(name = "bank_code", nullable = false, unique = true)
    private String bankCode; // e.g., routing swift/bic code

    @NotBlank
    @Size(max = 128)
    @Column(name = "bank_name", nullable = false)
    private String bankName;
}
