package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Wastage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WastageRepository extends JpaRepository<Wastage, Long> {

    List<Wastage> findByWastageDateBetween(LocalDate startDate, LocalDate endDate);

    List<Wastage> findByProductId(Long productId);

    List<Wastage> findByApproved(Boolean approved);

    @Query("SELECT w FROM Wastage w WHERE w.wastageDate >= :startDate AND w.wastageDate <= :endDate")
    List<Wastage> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(w.valueLost) FROM Wastage w WHERE w.approved = true AND w.wastageDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalValueLost(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}