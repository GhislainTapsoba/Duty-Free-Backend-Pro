package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.PassengerCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerCountRepository extends JpaRepository<PassengerCount, Long> {

    /**
     * Find passenger count by date
     */
    Optional<PassengerCount> findByCountDate(LocalDate date);

    /**
     * Find passenger counts between dates
     */
    List<PassengerCount> findByCountDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find passenger counts by flight number
     */
    List<PassengerCount> findByFlightNumber(String flightNumber);

    /**
     * Find passenger counts by airline
     */
    List<PassengerCount> findByAirline(String airline);

    /**
     * Get total passengers between dates
     */
    @Query("SELECT COALESCE(SUM(pc.totalPassengers), 0) FROM PassengerCount pc " +
           "WHERE pc.countDate BETWEEN :startDate AND :endDate")
    Integer getTotalPassengersBetween(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * Get average daily passengers between dates
     */
    @Query("SELECT COALESCE(AVG(pc.totalPassengers), 0) FROM PassengerCount pc " +
           "WHERE pc.countDate BETWEEN :startDate AND :endDate")
    Double getAverageDailyPassengers(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * Check if count exists for date
     */
    boolean existsByCountDate(LocalDate date);
}
