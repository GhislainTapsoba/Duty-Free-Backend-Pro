package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {

    Optional<ProductBundle> findByBundleCode(String bundleCode);

    List<ProductBundle> findByActiveTrue();

    List<ProductBundle> findByBundleType(String bundleType);

    @Query("SELECT pb FROM ProductBundle pb WHERE pb.active = true " +
           "AND (pb.validFrom IS NULL OR pb.validFrom <= :now) " +
           "AND (pb.validUntil IS NULL OR pb.validUntil >= :now)")
    List<ProductBundle> findActiveBundles(@Param("now") LocalDateTime now);

    @Query("SELECT pb FROM ProductBundle pb WHERE pb.category.id = :categoryId AND pb.active = true")
    List<ProductBundle> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT pb FROM ProductBundle pb " +
           "LEFT JOIN FETCH pb.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE pb.id = :id")
    Optional<ProductBundle> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT pb FROM ProductBundle pb " +
           "LEFT JOIN FETCH pb.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE pb.bundleCode = :bundleCode")
    Optional<ProductBundle> findByBundleCodeWithItems(@Param("bundleCode") String bundleCode);

    @Query("SELECT pb FROM ProductBundle pb " +
           "LEFT JOIN FETCH pb.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE pb.active = true")
    List<ProductBundle> findAllActiveWithItems();

    boolean existsByBundleCode(String bundleCode);
}
