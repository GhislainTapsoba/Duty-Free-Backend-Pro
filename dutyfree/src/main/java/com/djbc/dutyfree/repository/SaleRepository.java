package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findBySaleNumber(String saleNumber);

    Boolean existsBySaleNumber(String saleNumber);

    List<Sale> findByStatus(SaleStatus status);

    // ✅ CORRIGÉ : Utilisation de JPQL
    @Query("SELECT s FROM Sale s WHERE s.cashier.id = :cashierId")
    List<Sale> findByCashierId(@Param("cashierId") Long cashierId);

    // ✅ CORRIGÉ : Utilisation de JPQL
    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId")
    List<Sale> findByCustomerId(@Param("customerId") Long customerId);

    // ✅ CORRIGÉ : Utilisation de JPQL
    @Query("SELECT s FROM Sale s WHERE s.cashRegister.id = :cashRegisterId")
    List<Sale> findByCashRegisterId(@Param("cashRegisterId") Long cashRegisterId);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false ORDER BY s.saleDate DESC")
    Page<Sale> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.items LEFT JOIN FETCH s.payments WHERE s.id = :id")
    Optional<Sale> findByIdWithDetails(@Param("id") Long id);

    // Analytics queries
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND s.deleted = false")
    BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND s.deleted = false")
    Long countSalesBetween(@Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND s.deleted = false")
    BigDecimal getAverageTicketBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.cashier.id = :cashierId " +
            "AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false ORDER BY s.saleDate DESC")
    List<Sale> findByCashierAndDateBetween(@Param("cashierId") Long cashierId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.cashRegister.id = :cashRegisterId " +
            "AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false ORDER BY s.saleDate DESC")
    List<Sale> findByCashRegisterAndDateBetween(@Param("cashRegisterId") Long cashRegisterId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = :status AND s.deleted = false ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetweenAndStatus(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("status") SaleStatus status);
}