package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    Optional<Receipt> findBySaleId(Long saleId);

    Boolean existsByReceiptNumber(String receiptNumber);

    @Query("SELECT r FROM Receipt r WHERE r.printedDate BETWEEN :startDate AND :endDate " +
            "AND r.deleted = false")
    List<Receipt> findByPrintedDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Receipt r WHERE r.printed = false AND r.deleted = false")
    List<Receipt> findUnprintedReceipts();

    @Query("SELECT r FROM Receipt r WHERE r.emailed = false " +
            "AND r.emailAddress IS NOT NULL AND r.deleted = false")
    List<Receipt> findUnsentEmailReceipts();
}