package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.TechnicalSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicalSheetRepository extends JpaRepository<TechnicalSheet, Long> {

    Optional<TechnicalSheet> findBySheetCode(String sheetCode);

    Optional<TechnicalSheet> findByProductId(Long productId);

    List<TechnicalSheet> findByActiveTrue();

    List<TechnicalSheet> findByValidatedTrue();

    List<TechnicalSheet> findByValidatedFalse();

    @Query("SELECT ts FROM TechnicalSheet ts " +
           "LEFT JOIN FETCH ts.items i " +
           "LEFT JOIN FETCH i.rawMaterial " +
           "WHERE ts.id = :id")
    Optional<TechnicalSheet> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT ts FROM TechnicalSheet ts " +
           "LEFT JOIN FETCH ts.items i " +
           "LEFT JOIN FETCH i.rawMaterial " +
           "WHERE ts.sheetCode = :sheetCode")
    Optional<TechnicalSheet> findBySheetCodeWithItems(@Param("sheetCode") String sheetCode);

    @Query("SELECT ts FROM TechnicalSheet ts " +
           "LEFT JOIN FETCH ts.items i " +
           "LEFT JOIN FETCH i.rawMaterial " +
           "WHERE ts.product.id = :productId")
    Optional<TechnicalSheet> findByProductIdWithItems(@Param("productId") Long productId);

    boolean existsBySheetCode(String sheetCode);

    boolean existsByProductId(Long productId);
}
