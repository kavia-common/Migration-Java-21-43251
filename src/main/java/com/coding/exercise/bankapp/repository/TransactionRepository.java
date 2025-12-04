package com.coding.exercise.bankapp.repository;

import com.coding.exercise.bankapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Transaction entity with history queries.
 */
// PUBLIC_INTERFACE
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transactions by owning account id (FK on Transaction.account).
     *
     * @param accountId account primary key
     * @return list of transactions
     */
    // PUBLIC_INTERFACE
    List<Transaction> findByAccount_Id(Long accountId);

    /**
     * Mirroring source app style: a compound finder that returns transactions matching either of two account ids.
     * Useful for transfer history when modeling from/to sides.
     *
     * Note: Our Transaction entity references a single Account (account_id). This method uses an OR across the same field
     * to support use cases where caller passes "from" and "to" account ids to retrieve combined history.
     *
     * @param fromAccountId source account id
     * @param toAccountId destination account id
     * @return list of transactions
     */
    // PUBLIC_INTERFACE
    List<Transaction> findByAccount_IdOrAccount_Id(Long fromAccountId, Long toAccountId);
}
