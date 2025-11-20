package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // ✅ CORRIGÉ : Utilisation de JPQL
    @Query("SELECT si FROM SaleItem si WHERE si.sale.id = :saleId")
    List<SaleItem> findBySaleId(@Param("saleId") Long saleId);

    // ✅ CORRIGÉ : Utilisation de JPQL
    @Query("SELECT si FROM SaleItem si WHERE si.product.id = :productId")
    List<SaleItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT si FROM SaleItem si WHERE si.sale.id = :saleId AND si.deleted = false")
    List<SaleItem> findActiveBySaleId(@Param("saleId") Long saleId);

    @Query("SELECT si FROM SaleItem si JOIN si.sale s " +
            "WHERE si.product.id = :productId " +
            "AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND si.deleted = false")
    List<SaleItem> findByProductAndDateBetween(@Param("productId") Long productId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(si.quantity) FROM SaleItem si JOIN si.sale s " +
            "WHERE si.product.id = :productId " +
            "AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND si.deleted = false")
    Integer getTotalQuantitySoldBetween(@Param("productId") Long productId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}