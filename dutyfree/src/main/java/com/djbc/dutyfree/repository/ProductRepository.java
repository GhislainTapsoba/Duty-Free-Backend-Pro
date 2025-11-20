package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    Boolean existsBySku(String sku);

    Boolean existsByBarcode(String barcode);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryName(String categoryName);

    List<Product> findBySupplierId(Long supplierId);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.active = true")
    List<Product> findAllActiveProducts();

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.stocks s " +
            "WHERE p.deleted = false " +
            "GROUP BY p.id " +
            "HAVING COALESCE(SUM(s.availableQuantity), 0) <= p.minStockLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.stocks s " +
            "WHERE p.deleted = false " +
            "GROUP BY p.id " +
            "HAVING COALESCE(SUM(s.availableQuantity), 0) <= p.reorderLevel")
    List<Product> findProductsNeedingReorder();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
            "(LOWER(p.nameFr) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.stocks s " +
            "WHERE p.deleted = false AND p.id = :productId " +
            "GROUP BY p.id")
    Optional<Product> findByIdWithStock(@Param("productId") Long productId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.deleted = false")
    Long countActiveProducts();
}