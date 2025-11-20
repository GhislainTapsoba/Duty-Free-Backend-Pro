package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.CategoryRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.CategoryResponse;
import com.djbc.dutyfree.domain.entity.Category;
import com.djbc.dutyfree.domain.entity.Category;
import com.djbc.dutyfree.domain.entity.Supplier;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CategoryRepository;
import com.djbc.dutyfree.repository.CategoryRepository;
import com.djbc.dutyfree.repository.StockRepository;
import com.djbc.dutyfree.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(c -> CategoryResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .description(c.getDescription())
                .active(c.getActive())
                .build())
            .toList();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new BadRequestException("Category with name " + category.getName() + " already exists");
        }

        category = categoryRepository.save(category);
        log.info("Category created: {}", category.getName());
        return category;
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return CategoryResponse.builder()
            .id(category.getId())
            .code(category.getCode())
            .name(category.getName())
            .description(category.getDescription())
            .active(category.getActive())
            .build();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if another category with the same name exists
        if (categoryRepository.existsByName(categoryRequest.getNameFr()) &&
            !category.getName().equals(categoryRequest.getNameFr())) {
            throw new BadRequestException("Category with name " + categoryRequest.getNameFr() + " already exists");
        }

        category.setCode(categoryRequest.getCode());
        category.setName(categoryRequest.getNameFr());
        category.setDescription(categoryRequest.getDescriptionFr());
        category.setActive(categoryRequest.getActive() != null ? categoryRequest.getActive() : true);

        category = categoryRepository.save(category);
        log.info("Category updated: {}", category.getName());

        return CategoryResponse.builder()
            .id(category.getId())
            .code(category.getCode())
            .name(category.getName())
            .description(category.getDescription())
            .active(category.getActive())
            .build();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft delete
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Category soft deleted: {}", category.getName());
    }
}
