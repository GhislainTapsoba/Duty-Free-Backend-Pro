package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.RawMaterialRequest;
import com.djbc.dutyfree.domain.dto.response.RawMaterialResponse;
import com.djbc.dutyfree.domain.entity.RawMaterial;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;

    /**
     * Create a new raw material
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse createRawMaterial(RawMaterialRequest request) {
        log.info("Creating new raw material: {}", request.getMaterialCode());

        // Check if material code already exists
        if (rawMaterialRepository.existsByMaterialCode(request.getMaterialCode())) {
            throw new IllegalArgumentException("Material code already exists: " + request.getMaterialCode());
        }

        RawMaterial rawMaterial = mapToEntity(request);
        RawMaterial savedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Raw material created successfully: {} - {}", savedMaterial.getId(), savedMaterial.getMaterialCode());
        return mapToResponse(savedMaterial);
    }

    /**
     * Update an existing raw material
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse updateRawMaterial(Long id, RawMaterialRequest request) {
        log.info("Updating raw material: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        // Check if material code is being changed and if the new code already exists
        if (!rawMaterial.getMaterialCode().equals(request.getMaterialCode())) {
            if (rawMaterialRepository.existsByMaterialCode(request.getMaterialCode())) {
                throw new IllegalArgumentException("Material code already exists: " + request.getMaterialCode());
            }
        }

        updateEntityFromRequest(rawMaterial, request);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Raw material updated successfully: {}", id);
        return mapToResponse(updatedMaterial);
    }

    /**
     * Get raw material by ID
     */
    @Cacheable(value = "rawMaterials", key = "#id")
    @Transactional(readOnly = true)
    public RawMaterialResponse getRawMaterialById(Long id) {
        log.debug("Fetching raw material by ID: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        return mapToResponse(rawMaterial);
    }

    /**
     * Get raw material by material code
     */
    @Cacheable(value = "rawMaterials", key = "'code_' + #materialCode")
    @Transactional(readOnly = true)
    public RawMaterialResponse getRawMaterialByCode(String materialCode) {
        log.debug("Fetching raw material by code: {}", materialCode);

        RawMaterial rawMaterial = rawMaterialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with code: " + materialCode));

        return mapToResponse(rawMaterial);
    }

    /**
     * Get all raw materials
     */
    @Cacheable(value = "rawMaterials", key = "'all'")
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getAllRawMaterials() {
        log.debug("Fetching all raw materials");

        return rawMaterialRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active raw materials
     */
    @Cacheable(value = "rawMaterials", key = "'active'")
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getActiveRawMaterials() {
        log.debug("Fetching active raw materials");

        return rawMaterialRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get raw materials by category
     */
    @Cacheable(value = "rawMaterials", key = "'category_' + #category")
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getRawMaterialsByCategory(String category) {
        log.debug("Fetching raw materials by category: {}", category);

        return rawMaterialRepository.findByMaterialCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock raw materials
     */
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getLowStockMaterials() {
        log.debug("Fetching low stock raw materials");

        return rawMaterialRepository.findLowStock().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get raw materials needing reorder
     */
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getMaterialsNeedingReorder() {
        log.debug("Fetching raw materials needing reorder");

        return rawMaterialRepository.findNeedingReorder().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get perishable raw materials
     */
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getPerishableMaterials() {
        log.debug("Fetching perishable raw materials");

        return rawMaterialRepository.findPerishable().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a raw material (soft delete)
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public void deleteRawMaterial(Long id) {
        log.info("Deleting raw material: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        rawMaterial.setDeleted(true);
        rawMaterial.setActive(false);
        rawMaterialRepository.save(rawMaterial);

        log.info("Raw material deleted successfully: {}", id);
    }

    /**
     * Update stock quantity
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse updateStock(Long id, BigDecimal quantity) {
        log.info("Updating stock for raw material: {} with quantity: {}", id, quantity);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        rawMaterial.setQuantityInStock(quantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Stock updated successfully for raw material: {}", id);
        return mapToResponse(updatedMaterial);
    }

    /**
     * Add to stock
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse addToStock(Long id, BigDecimal quantityToAdd) {
        log.info("Adding to stock for raw material: {} quantity: {}", id, quantityToAdd);

        if (quantityToAdd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to add must be greater than 0");
        }

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        BigDecimal newQuantity = rawMaterial.getQuantityInStock().add(quantityToAdd);
        rawMaterial.setQuantityInStock(newQuantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Added {} to stock for raw material: {}. New quantity: {}", quantityToAdd, id, newQuantity);
        return mapToResponse(updatedMaterial);
    }

    /**
     * Reduce from stock
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse reduceFromStock(Long id, BigDecimal quantityToReduce) {
        log.info("Reducing from stock for raw material: {} quantity: {}", id, quantityToReduce);

        if (quantityToReduce.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to reduce must be greater than 0");
        }

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        BigDecimal newQuantity = rawMaterial.getQuantityInStock().subtract(quantityToReduce);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + rawMaterial.getQuantityInStock() + ", requested: " + quantityToReduce);
        }

        rawMaterial.setQuantityInStock(newQuantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Reduced {} from stock for raw material: {}. New quantity: {}", quantityToReduce, id, newQuantity);
        return mapToResponse(updatedMaterial);
    }

    /**
     * Activate a raw material
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse activateRawMaterial(Long id) {
        log.info("Activating raw material: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        rawMaterial.setActive(true);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Raw material activated successfully: {}", id);
        return mapToResponse(updatedMaterial);
    }

    /**
     * Deactivate a raw material
     */
    @CacheEvict(value = "rawMaterials", allEntries = true)
    public RawMaterialResponse deactivateRawMaterial(Long id) {
        log.info("Deactivating raw material: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + id));

        rawMaterial.setActive(false);
        RawMaterial updatedMaterial = rawMaterialRepository.save(rawMaterial);

        log.info("Raw material deactivated successfully: {}", id);
        return mapToResponse(updatedMaterial);
    }

    // Helper methods

    private RawMaterial mapToEntity(RawMaterialRequest request) {
        return RawMaterial.builder()
                .materialCode(request.getMaterialCode())
                .materialName(request.getMaterialName())
                .description(request.getDescription())
                .materialCategory(request.getMaterialCategory())
                .unit(request.getUnit())
                .purchasePrice(request.getPurchasePrice())
                .quantityInStock(request.getQuantityInStock())
                .minStockLevel(request.getMinStockLevel())
                .reorderLevel(request.getReorderLevel())
                .reorderQuantity(request.getReorderQuantity())
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .perishable(request.getPerishable())
                .shelfLifeDays(request.getShelfLifeDays())
                .storageConditions(request.getStorageConditions())
                .lastPurchaseDate(request.getLastPurchaseDate())
                .lastPurchasePrice(request.getLastPurchasePrice())
                .notes(request.getNotes())
                .active(request.getActive())
                .build();
    }

    private void updateEntityFromRequest(RawMaterial rawMaterial, RawMaterialRequest request) {
        rawMaterial.setMaterialCode(request.getMaterialCode());
        rawMaterial.setMaterialName(request.getMaterialName());
        rawMaterial.setDescription(request.getDescription());
        rawMaterial.setMaterialCategory(request.getMaterialCategory());
        rawMaterial.setUnit(request.getUnit());
        rawMaterial.setPurchasePrice(request.getPurchasePrice());
        rawMaterial.setQuantityInStock(request.getQuantityInStock());
        rawMaterial.setMinStockLevel(request.getMinStockLevel());
        rawMaterial.setReorderLevel(request.getReorderLevel());
        rawMaterial.setReorderQuantity(request.getReorderQuantity());
        rawMaterial.setSupplierName(request.getSupplierName());
        rawMaterial.setSupplierContact(request.getSupplierContact());
        rawMaterial.setPerishable(request.getPerishable());
        rawMaterial.setShelfLifeDays(request.getShelfLifeDays());
        rawMaterial.setStorageConditions(request.getStorageConditions());
        rawMaterial.setLastPurchaseDate(request.getLastPurchaseDate());
        rawMaterial.setLastPurchasePrice(request.getLastPurchasePrice());
        rawMaterial.setNotes(request.getNotes());
        rawMaterial.setActive(request.getActive());
    }

    private RawMaterialResponse mapToResponse(RawMaterial rawMaterial) {
        RawMaterialResponse response = RawMaterialResponse.builder()
                .id(rawMaterial.getId())
                .materialCode(rawMaterial.getMaterialCode())
                .materialName(rawMaterial.getMaterialName())
                .description(rawMaterial.getDescription())
                .materialCategory(rawMaterial.getMaterialCategory())
                .unit(rawMaterial.getUnit())
                .purchasePrice(rawMaterial.getPurchasePrice())
                .quantityInStock(rawMaterial.getQuantityInStock())
                .minStockLevel(rawMaterial.getMinStockLevel())
                .reorderLevel(rawMaterial.getReorderLevel())
                .reorderQuantity(rawMaterial.getReorderQuantity())
                .supplierName(rawMaterial.getSupplierName())
                .supplierContact(rawMaterial.getSupplierContact())
                .perishable(rawMaterial.getPerishable())
                .shelfLifeDays(rawMaterial.getShelfLifeDays())
                .storageConditions(rawMaterial.getStorageConditions())
                .lastPurchaseDate(rawMaterial.getLastPurchaseDate())
                .lastPurchasePrice(rawMaterial.getLastPurchasePrice())
                .notes(rawMaterial.getNotes())
                .active(rawMaterial.getActive())
                .createdAt(rawMaterial.getCreatedAt())
                .updatedAt(rawMaterial.getUpdatedAt())
                .createdBy(rawMaterial.getCreatedBy())
                .updatedBy(rawMaterial.getUpdatedBy())
                .build();

        // Calculate computed fields
        if (rawMaterial.getMinStockLevel() != null && rawMaterial.getQuantityInStock() != null) {
            response.setIsLowStock(rawMaterial.getQuantityInStock().compareTo(rawMaterial.getMinStockLevel()) <= 0);
        }

        if (rawMaterial.getReorderLevel() != null && rawMaterial.getQuantityInStock() != null) {
            response.setNeedsReorder(rawMaterial.getQuantityInStock().compareTo(rawMaterial.getReorderLevel()) <= 0);
        }

        if (rawMaterial.getQuantityInStock() != null && rawMaterial.getPurchasePrice() != null) {
            response.setStockValue(rawMaterial.getQuantityInStock().multiply(rawMaterial.getPurchasePrice()));
        }

        return response;
    }
}
