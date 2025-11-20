package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.ScheduledPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduledPriceRepository extends JpaRepository<ScheduledPrice, Long> {

    /**
     * Find all scheduled prices for a product
     */
    List<ScheduledPrice> findByProductId(Long productId);

    /**
     * Find active scheduled prices for a product
     */
    @Query("SELECT sp FROM ScheduledPrice sp WHERE sp.product.id = :productId AND sp.active = true AND sp.deleted = false")
    List<ScheduledPrice> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Find currently valid prices for a product (considering date, time, and day of week)
     */
    @Query("SELECT sp FROM ScheduledPrice sp " +
           "WHERE sp.product.id = :productId " +
           "AND sp.active = true " +
           "AND sp.deleted = false " +
           "AND (sp.validFrom IS NULL OR sp.validFrom <= :currentDate) " +
           "AND (sp.validUntil IS NULL OR sp.validUntil >= :currentDate) " +
           "ORDER BY sp.priority DESC")
    List<ScheduledPrice> findCurrentlyValidForProduct(@Param("productId") Long productId,
                                                       @Param("currentDate") LocalDate currentDate);

    /**
     * Find all active scheduled prices
     */
    @Query("SELECT sp FROM ScheduledPrice sp WHERE sp.active = true AND sp.deleted = false")
    List<ScheduledPrice> findAllActive();

    /**
     * Find scheduled prices by period type
     */
    @Query("SELECT sp FROM ScheduledPrice sp WHERE sp.periodType = :periodType AND sp.deleted = false")
    List<ScheduledPrice> findByPeriodType(@Param("periodType") String periodType);

    /**
     * Find scheduled prices valid in a date range
     */
    @Query("SELECT sp FROM ScheduledPrice sp " +
           "WHERE sp.deleted = false " +
           "AND ((sp.validFrom IS NULL OR sp.validFrom <= :endDate) " +
           "AND (sp.validUntil IS NULL OR sp.validUntil >= :startDate))")
    List<ScheduledPrice> findValidInDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Count active prices for a product
     */
    @Query("SELECT COUNT(sp) FROM ScheduledPrice sp " +
           "WHERE sp.product.id = :productId AND sp.active = true AND sp.deleted = false")
    Long countActiveByProductId(@Param("productId") Long productId);
}
