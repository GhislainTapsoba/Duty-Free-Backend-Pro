package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.TerminalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalTransactionRepository extends JpaRepository<TerminalTransaction, Long> {

    Optional<TerminalTransaction> findByTransactionId(String transactionId);

    @Query("SELECT tt FROM TerminalTransaction tt WHERE tt.terminal.id = :terminalId ORDER BY tt.createdAt DESC")
    List<TerminalTransaction> findByTerminalId(@Param("terminalId") Long terminalId);

    @Query("SELECT tt FROM TerminalTransaction tt WHERE tt.payment.id = :paymentId")
    Optional<TerminalTransaction> findByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT tt FROM TerminalTransaction tt " +
           "WHERE tt.status = :status " +
           "ORDER BY tt.createdAt DESC")
    List<TerminalTransaction> findByStatus(@Param("status") TerminalTransaction.TransactionStatus status);

    @Query("SELECT tt FROM TerminalTransaction tt " +
           "WHERE tt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY tt.createdAt DESC")
    List<TerminalTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tt FROM TerminalTransaction tt " +
           "WHERE tt.terminal.id = :terminalId " +
           "AND tt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY tt.createdAt DESC")
    List<TerminalTransaction> findByTerminalIdAndDateRange(@Param("terminalId") Long terminalId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(tt) FROM TerminalTransaction tt " +
           "WHERE tt.status = 'APPROVED' " +
           "AND tt.createdAt BETWEEN :startDate AND :endDate")
    Long countSuccessfulTransactions(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(tt) FROM TerminalTransaction tt " +
           "WHERE tt.status IN ('DECLINED', 'ERROR', 'TIMEOUT') " +
           "AND tt.createdAt BETWEEN :startDate AND :endDate")
    Long countFailedTransactions(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(tt.amount), 0) FROM TerminalTransaction tt " +
           "WHERE tt.status = 'APPROVED' " +
           "AND tt.transactionType = 'SALE' " +
           "AND tt.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalSalesAmount(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(AVG(tt.responseTimeMs), 0) FROM TerminalTransaction tt " +
           "WHERE tt.responseTimeMs IS NOT NULL " +
           "AND tt.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageResponseTime(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tt FROM TerminalTransaction tt " +
           "WHERE tt.status = 'PENDING' " +
           "AND tt.startedAt < :timeout " +
           "ORDER BY tt.startedAt ASC")
    List<TerminalTransaction> findTimedOutTransactions(@Param("timeout") LocalDateTime timeout);
}
