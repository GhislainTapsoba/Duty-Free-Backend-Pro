package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SommierRepository extends JpaRepository<Sommier, Long> {

    Optional<Sommier> findBySommierNumber(String sommierNumber);

    Boolean existsBySommierNumber(String sommierNumber);

    List<Sommier> findByStatus(SommierStatus status);

    List<Sommier> findByPurchaseOrderId(Long purchaseOrderId);

    @Query("SELECT s FROM Sommier s WHERE s.status = :status AND s.deleted = false")
    List<Sommier> findActiveSommiersByStatus(@Param("status") SommierStatus status);

    @Query("SELECT s FROM Sommier s WHERE s.alertDate <= :date " +
            "AND s.alertSent = false AND s.status = 'ACTIVE' AND s.deleted = false")
    List<Sommier> findSommiersNeedingAlert(@Param("date") LocalDate date);

    @Query("SELECT s FROM Sommier s WHERE s.openingDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false")
    List<Sommier> findByOpeningDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Sommier s LEFT JOIN FETCH s.stocks WHERE s.id = :id")
    Optional<Sommier> findByIdWithStocks(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Sommier s WHERE s.status = 'ACTIVE' AND s.deleted = false")
    Long countActiveSommiers();
}