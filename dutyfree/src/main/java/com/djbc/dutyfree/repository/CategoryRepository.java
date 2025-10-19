package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCode(String code);

    Optional<Category> findByName(String name);

    Boolean existsByCode(String code);

    Boolean existsByName(String name);

    List<Category> findByActiveTrue();

    @Query("SELECT c FROM Category c WHERE c.deleted = false AND c.parent IS NULL")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.deleted = false AND c.parent.id = :parentId")
    List<Category> findSubCategories(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubCategories(@Param("id") Long id);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);
}