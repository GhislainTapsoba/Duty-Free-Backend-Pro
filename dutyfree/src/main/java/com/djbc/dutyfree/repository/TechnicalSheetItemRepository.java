package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.TechnicalSheetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalSheetItemRepository extends JpaRepository<TechnicalSheetItem, Long> {

    List<TechnicalSheetItem> findByTechnicalSheetId(Long technicalSheetId);

    @Query("SELECT tsi FROM TechnicalSheetItem tsi WHERE tsi.rawMaterial.id = :rawMaterialId")
    List<TechnicalSheetItem> findByRawMaterialId(@Param("rawMaterialId") Long rawMaterialId);

    @Query("SELECT tsi FROM TechnicalSheetItem tsi " +
           "WHERE tsi.technicalSheet.id = :technicalSheetId " +
           "ORDER BY tsi.displayOrder ASC, tsi.id ASC")
    List<TechnicalSheetItem> findByTechnicalSheetIdOrderByDisplayOrder(@Param("technicalSheetId") Long technicalSheetId);

    void deleteByTechnicalSheetId(Long technicalSheetId);
}
