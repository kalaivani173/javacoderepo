package com.npci.UPISim.repo;

import com.npci.UPISim.model.TransactionLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {


    @Query("SELECT t.txnId FROM TransactionLog t GROUP BY t.txnId ORDER BY MAX(t.createdAt) DESC")
    List<String> findDistinctTxnIds();

    @Query("SELECT t.payload FROM TransactionLog t WHERE t.txnId = :txnId ORDER BY t.createdAt ASC")
    List<String> findPayloadsByTxnId(@Param("txnId") String txnId);

    Optional<TransactionLog> findTopByTxnIdAndApiAndDirectionOrderByCreatedAtDesc(String txnId, String api, String direction);

    @Modifying
    @Transactional
    @Query("UPDATE TransactionLog t SET t.status = :status WHERE t.txnId = :txnId")
    void updateTxnStatus(@Param("txnId") String txnId, @Param("status") String status);

    List<TransactionLog> findByTxnIdOrderByCreatedAtAsc(String txnId);

    @Query(value = "SELECT status FROM transaction_logs WHERE txnId = :txnId ORDER BY createdAt DESC LIMIT 1", nativeQuery = true)
    String findLatestStatus(@Param("txnId") String txnId);

    // Native query that returns distinct txnId and latest FinalRespPay status (or 'PENDING' if none).
    @Query(
            value = """
          SELECT 
              t.txnId AS txnId,
              COALESCE((
                  SELECT r.status 
                  FROM transaction_logs r 
                  WHERE r.txnId = t.txnId AND r.api = 'FinalRespPay'
                  ORDER BY r.createdAt DESC LIMIT 1
              ), 'PENDING') AS respStatus,
              COALESCE((
                  SELECT r.createdAt 
                  FROM transaction_logs r 
                  WHERE r.txnId = t.txnId AND r.api = 'FinalRespPay'
                  ORDER BY r.createdAt DESC LIMIT 1
              ), MAX(t.createdAt)) AS createdAt
          FROM transaction_logs t
          GROUP BY t.txnId
          ORDER BY MAX(t.createdAt) DESC
          """,
            nativeQuery = true)
    List<TxnRespStatusProjection> findAllTxnIdsWithLatestFinalRespPayStatus();
}

