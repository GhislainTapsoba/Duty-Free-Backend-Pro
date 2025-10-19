package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByProductId(Long productId);

    List<Stock> findBySommierId(Long sommierId);

    Optional<Stock> findByProductIdAndLocation(Long productId, String location);

    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId AND s.deleted = false")
    List<Stock> findActiveStocksByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(s.availableQuantity) FROM Stock s " +
            "WHERE s.product.id = :productId AND s.deleted = false")
    Integer getTotalAvailableQuantity(@Param("productId") Long productId);

    @Query("SELECT SUM(s.quantity) FROM Stock s " +
            "WHERE s.product.id = :productId AND s.deleted = false")
    Integer getTotalQuantity(@Param("productId") Long productId);

    @Query("SELECT s FROM Stock s WHERE s.expiryDate IS NOT NULL " +
            "AND s.expiryDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false")
    List<Stock> findExpiringStock(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Stock s WHERE s.expiryDate IS NOT NULL " +
            "AND s.expiryDate < :date AND s.deleted = false")
    List<Stock> findExpiredStock(@Param("date") LocalDate date);

    @Query("SELECT s FROM Stock s WHERE s.location = :location AND s.deleted = false")
    List<Stock> findByLocation(@Param("location") String location);
}