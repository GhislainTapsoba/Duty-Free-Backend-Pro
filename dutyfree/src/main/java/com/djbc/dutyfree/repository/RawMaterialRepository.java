package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {

    Optional<RawMaterial> findByMaterialCode(String materialCode);

    List<RawMaterial> findByActiveTrue();

    List<RawMaterial> findByMaterialCategory(String materialCategory);

    @Query("SELECT rm FROM RawMaterial rm WHERE rm.quantityInStock <= rm.minStockLevel AND rm.active = true")
    List<RawMaterial> findLowStock();

    @Query("SELECT rm FROM RawMaterial rm WHERE rm.quantityInStock <= rm.reorderLevel AND rm.active = true")
    List<RawMaterial> findNeedingReorder();

    @Query("SELECT rm FROM RawMaterial rm WHERE rm.perishable = true AND rm.active = true")
    List<RawMaterial> findPerishable();

    boolean existsByMaterialCode(String materialCode);
}
