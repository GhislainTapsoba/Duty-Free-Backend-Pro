package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    Optional<Supplier> findByName(String name);

    Boolean existsByCode(String code);

    Boolean existsByName(String name);

    List<Supplier> findByActiveTrue();

    List<Supplier> findByCountry(String country);

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false AND " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Supplier> searchSuppliers(@Param("search") String search);

    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.purchaseOrders WHERE s.id = :id")
    Optional<Supplier> findByIdWithOrders(@Param("id") Long id);
}