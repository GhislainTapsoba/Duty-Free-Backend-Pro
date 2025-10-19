package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.ExchangeRate;
import com.djbc.dutyfree.domain.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT er FROM ExchangeRate er WHERE er.currency = :currency " +
            "AND er.effectiveDate <= :date " +
            "AND (er.expiryDate IS NULL OR er.expiryDate >= :date) " +
            "AND er.active = true AND er.deleted = false " +
            "ORDER BY er.effectiveDate DESC")
    Optional<ExchangeRate> findActiveByCurrencyAndDate(@Param("currency") Currency currency,
                                                       @Param("date") LocalDate date);

    List<ExchangeRate> findByCurrency(Currency currency);

    List<ExchangeRate> findByActiveTrue();

    @Query("SELECT er FROM ExchangeRate er WHERE er.effectiveDate BETWEEN :startDate AND :endDate " +
            "AND er.deleted = false ORDER BY er.effectiveDate DESC")
    List<ExchangeRate> findByEffectiveDateBetween(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT er FROM ExchangeRate er WHERE er.currency = :currency " +
            "ORDER BY er.effectiveDate DESC")
    List<ExchangeRate> findLatestByCurrency(@Param("currency") Currency currency);
}