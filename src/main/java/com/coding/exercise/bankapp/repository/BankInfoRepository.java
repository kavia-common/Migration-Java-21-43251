package com.coding.exercise.bankapp.repository;

import com.coding.exercise.bankapp.model.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for BankInfo entity to enable lookups and persistence of bank metadata.
 */
// PUBLIC_INTERFACE
public interface BankInfoRepository extends JpaRepository<BankInfo, Long> {

    /**
     * Find a BankInfo by its unique bankCode.
     *
     * @param bankCode bank code (e.g. routing/SWIFT/BIC)
     * @return optional bank info
     */
    // PUBLIC_INTERFACE
    Optional<BankInfo> findByBankCode(String bankCode);
}
