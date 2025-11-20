package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SommierRepository extends JpaRepository<Sommier, Long> {

    Optional<Sommier> findBySommierNumber(String sommierNumber);

    List<Sommier> findByStatus(SommierStatus status);

    Long countByStatus(SommierStatus status);

    List<Sommier> findByStatusAndCurrentValueGreaterThan(SommierStatus status, BigDecimal value);

    @Query("SELECT s FROM Sommier s WHERE s.openingDate BETWEEN CAST(:startDate AS LocalDate) AND CAST(:endDate AS LocalDate) ORDER BY s.openingDate DESC")
    List<Sommier> findByDeclarationDateBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}
