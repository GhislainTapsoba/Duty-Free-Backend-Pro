package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.StockMovement;
import com.djbc.dutyfree.domain.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.deleted = false ORDER BY sm.movementDate DESC")
    List<StockMovement> findByProductId(@Param("productId") Long productId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.type = :type AND sm.deleted = false ORDER BY sm.movementDate DESC")
    List<StockMovement> findByType(@Param("type") MovementType type);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.deleted = false ORDER BY sm.movementDate DESC")
    List<StockMovement> findAll();
}