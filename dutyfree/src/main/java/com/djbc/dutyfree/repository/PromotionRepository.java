package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    Boolean existsByCode(String code);

    List<Promotion> findByActiveTrue();

    @Query("SELECT p FROM Promotion p WHERE p.active = true " +
            "AND p.startDate <= :now AND p.endDate >= :now " +
            "AND p.deleted = false")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p " +
            "JOIN p.applicableProducts prod " +
            "WHERE prod.id = :productId " +
            "AND p.active = true " +
            "AND p.startDate <= :now AND p.endDate >= :now " +
            "AND p.deleted = false")
    List<Promotion> findActivePromotionsForProduct(@Param("productId") Long productId,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p LEFT JOIN FETCH p.applicableProducts WHERE p.id = :id")
    Optional<Promotion> findByIdWithProducts(@Param("id") Long id);
}