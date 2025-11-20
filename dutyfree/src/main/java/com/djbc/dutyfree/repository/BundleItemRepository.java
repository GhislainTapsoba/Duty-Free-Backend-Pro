package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {

    List<BundleItem> findByBundleId(Long bundleId);

    @Query("SELECT bi FROM BundleItem bi WHERE bi.product.id = :productId")
    List<BundleItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT bi FROM BundleItem bi " +
           "WHERE bi.bundle.id = :bundleId " +
           "ORDER BY bi.displayOrder ASC, bi.id ASC")
    List<BundleItem> findByBundleIdOrderByDisplayOrder(@Param("bundleId") Long bundleId);

    void deleteByBundleId(Long bundleId);
}
