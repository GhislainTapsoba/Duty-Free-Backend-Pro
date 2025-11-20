package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.TechnicalSheetItemRequest;
import com.djbc.dutyfree.domain.dto.request.TechnicalSheetRequest;
import com.djbc.dutyfree.domain.dto.response.TechnicalSheetItemResponse;
import com.djbc.dutyfree.domain.dto.response.TechnicalSheetResponse;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.RawMaterial;
import com.djbc.dutyfree.domain.entity.TechnicalSheet;
import com.djbc.dutyfree.domain.entity.TechnicalSheetItem;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.RawMaterialRepository;
import com.djbc.dutyfree.repository.TechnicalSheetItemRepository;
import com.djbc.dutyfree.repository.TechnicalSheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TechnicalSheetService {

    private final TechnicalSheetRepository technicalSheetRepository;
    private final TechnicalSheetItemRepository technicalSheetItemRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse createTechnicalSheet(TechnicalSheetRequest request) {
        log.info("Creating new technical sheet: {}", request.getSheetCode());

        // Check if sheet code already exists
        if (technicalSheetRepository.existsBySheetCode(request.getSheetCode())) {
            throw new IllegalArgumentException("Technical sheet code already exists: " + request.getSheetCode());
        }

        // Check if product is already associated with another technical sheet
        if (request.getProductId() != null && technicalSheetRepository.existsByProductId(request.getProductId())) {
            throw new IllegalArgumentException("Product already has a technical sheet");
        }

        TechnicalSheet technicalSheet = mapToEntity(request);

        // Add items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (TechnicalSheetItemRequest itemRequest : request.getItems()) {
                TechnicalSheetItem item = createTechnicalSheetItem(itemRequest);
                technicalSheet.addItem(item);
            }
        }

        // Calculate costs
        calculateItemCosts(technicalSheet);
        technicalSheet.calculateTotalCost();

        TechnicalSheet savedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet created successfully: {} - {}", savedSheet.getId(), savedSheet.getSheetCode());
        return mapToResponse(savedSheet);
    }

    /**
     * Update an existing technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse updateTechnicalSheet(Long id, TechnicalSheetRequest request) {
        log.info("Updating technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        // Check if sheet code is being changed and if the new code already exists
        if (!technicalSheet.getSheetCode().equals(request.getSheetCode())) {
            if (technicalSheetRepository.existsBySheetCode(request.getSheetCode())) {
                throw new IllegalArgumentException("Technical sheet code already exists: " + request.getSheetCode());
            }
        }

        // Check if product is being changed and if the new product already has a technical sheet
        if (request.getProductId() != null &&
            (technicalSheet.getProduct() == null || !technicalSheet.getProduct().getId().equals(request.getProductId()))) {
            if (technicalSheetRepository.existsByProductId(request.getProductId())) {
                throw new IllegalArgumentException("Product already has a technical sheet");
            }
        }

        updateEntityFromRequest(technicalSheet, request);

        // Update items
        technicalSheet.getItems().clear();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (TechnicalSheetItemRequest itemRequest : request.getItems()) {
                TechnicalSheetItem item = createTechnicalSheetItem(itemRequest);
                technicalSheet.addItem(item);
            }
        }

        // Recalculate costs
        calculateItemCosts(technicalSheet);
        technicalSheet.calculateTotalCost();

        TechnicalSheet updatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet updated successfully: {}", id);
        return mapToResponse(updatedSheet);
    }

    /**
     * Get technical sheet by ID
     */
    @Cacheable(value = "technicalSheets", key = "#id")
    @Transactional(readOnly = true)
    public TechnicalSheetResponse getTechnicalSheetById(Long id) {
        log.debug("Fetching technical sheet by ID: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        return mapToResponse(technicalSheet);
    }

    /**
     * Get technical sheet by code
     */
    @Cacheable(value = "technicalSheets", key = "'code_' + #sheetCode")
    @Transactional(readOnly = true)
    public TechnicalSheetResponse getTechnicalSheetByCode(String sheetCode) {
        log.debug("Fetching technical sheet by code: {}", sheetCode);

        TechnicalSheet technicalSheet = technicalSheetRepository.findBySheetCodeWithItems(sheetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with code: " + sheetCode));

        return mapToResponse(technicalSheet);
    }

    /**
     * Get technical sheet by product ID
     */
    @Cacheable(value = "technicalSheets", key = "'product_' + #productId")
    @Transactional(readOnly = true)
    public TechnicalSheetResponse getTechnicalSheetByProductId(Long productId) {
        log.debug("Fetching technical sheet by product ID: {}", productId);

        TechnicalSheet technicalSheet = technicalSheetRepository.findByProductIdWithItems(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found for product id: " + productId));

        return mapToResponse(technicalSheet);
    }

    /**
     * Get all technical sheets
     */
    @Cacheable(value = "technicalSheets", key = "'all'")
    @Transactional(readOnly = true)
    public List<TechnicalSheetResponse> getAllTechnicalSheets() {
        log.debug("Fetching all technical sheets");

        return technicalSheetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active technical sheets
     */
    @Cacheable(value = "technicalSheets", key = "'active'")
    @Transactional(readOnly = true)
    public List<TechnicalSheetResponse> getActiveTechnicalSheets() {
        log.debug("Fetching active technical sheets");

        return technicalSheetRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get validated technical sheets
     */
    @Cacheable(value = "technicalSheets", key = "'validated'")
    @Transactional(readOnly = true)
    public List<TechnicalSheetResponse> getValidatedTechnicalSheets() {
        log.debug("Fetching validated technical sheets");

        return technicalSheetRepository.findByValidatedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get draft (unvalidated) technical sheets
     */
    @Cacheable(value = "technicalSheets", key = "'drafts'")
    @Transactional(readOnly = true)
    public List<TechnicalSheetResponse> getDraftTechnicalSheets() {
        log.debug("Fetching draft technical sheets");

        return technicalSheetRepository.findByValidatedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate a technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse validateTechnicalSheet(Long id) {
        log.info("Validating technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        if (technicalSheet.getValidated()) {
            throw new IllegalArgumentException("Technical sheet is already validated");
        }

        technicalSheet.setValidated(true);
        technicalSheet.setValidatedAt(LocalDateTime.now());
        technicalSheet.setValidatedBy(getCurrentUsername());

        TechnicalSheet validatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet validated successfully: {}", id);
        return mapToResponse(validatedSheet);
    }

    /**
     * Unvalidate a technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse unvalidateTechnicalSheet(Long id) {
        log.info("Unvalidating technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        technicalSheet.setValidated(false);
        technicalSheet.setValidatedAt(null);
        technicalSheet.setValidatedBy(null);

        TechnicalSheet unvalidatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet unvalidated successfully: {}", id);
        return mapToResponse(unvalidatedSheet);
    }

    /**
     * Activate a technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse activateTechnicalSheet(Long id) {
        log.info("Activating technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        technicalSheet.setActive(true);
        TechnicalSheet activatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet activated successfully: {}", id);
        return mapToResponse(activatedSheet);
    }

    /**
     * Deactivate a technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse deactivateTechnicalSheet(Long id) {
        log.info("Deactivating technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        technicalSheet.setActive(false);
        TechnicalSheet deactivatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet deactivated successfully: {}", id);
        return mapToResponse(deactivatedSheet);
    }

    /**
     * Delete a technical sheet (soft delete)
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public void deleteTechnicalSheet(Long id) {
        log.info("Deleting technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        technicalSheet.setDeleted(true);
        technicalSheet.setActive(false);
        technicalSheetRepository.save(technicalSheet);

        log.info("Technical sheet deleted successfully: {}", id);
    }

    /**
     * Recalculate costs for a technical sheet
     */
    @CacheEvict(value = "technicalSheets", allEntries = true)
    public TechnicalSheetResponse recalculateCosts(Long id) {
        log.info("Recalculating costs for technical sheet: {}", id);

        TechnicalSheet technicalSheet = technicalSheetRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technical sheet not found with id: " + id));

        calculateItemCosts(technicalSheet);
        technicalSheet.calculateTotalCost();

        TechnicalSheet updatedSheet = technicalSheetRepository.save(technicalSheet);

        log.info("Costs recalculated successfully for technical sheet: {}", id);
        return mapToResponse(updatedSheet);
    }

    // Helper methods

    private TechnicalSheet mapToEntity(TechnicalSheetRequest request) {
        TechnicalSheet.TechnicalSheetBuilder builder = TechnicalSheet.builder()
                .sheetCode(request.getSheetCode())
                .name(request.getName())
                .description(request.getDescription())
                .version(request.getVersion())
                .outputQuantity(request.getOutputQuantity())
                .outputUnit(request.getOutputUnit())
                .preparationTime(request.getPreparationTime())
                .cookingTime(request.getCookingTime())
                .difficulty(request.getDifficulty())
                .instructions(request.getInstructions())
                .notes(request.getNotes())
                .active(request.getActive())
                .validated(request.getValidated());

        // Set product if provided
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            builder.product(product);
        }

        return builder.build();
    }

    private void updateEntityFromRequest(TechnicalSheet technicalSheet, TechnicalSheetRequest request) {
        technicalSheet.setSheetCode(request.getSheetCode());
        technicalSheet.setName(request.getName());
        technicalSheet.setDescription(request.getDescription());
        technicalSheet.setVersion(request.getVersion());
        technicalSheet.setOutputQuantity(request.getOutputQuantity());
        technicalSheet.setOutputUnit(request.getOutputUnit());
        technicalSheet.setPreparationTime(request.getPreparationTime());
        technicalSheet.setCookingTime(request.getCookingTime());
        technicalSheet.setDifficulty(request.getDifficulty());
        technicalSheet.setInstructions(request.getInstructions());
        technicalSheet.setNotes(request.getNotes());
        technicalSheet.setActive(request.getActive());
        technicalSheet.setValidated(request.getValidated());

        // Update product if provided
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            technicalSheet.setProduct(product);
        } else {
            technicalSheet.setProduct(null);
        }
    }

    private TechnicalSheetItem createTechnicalSheetItem(TechnicalSheetItemRequest request) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(request.getRawMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found with id: " + request.getRawMaterialId()));

        return TechnicalSheetItem.builder()
                .rawMaterial(rawMaterial)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .displayOrder(request.getDisplayOrder())
                .notes(request.getNotes())
                .optional(request.getOptional())
                .conversionFactor(request.getConversionFactor())
                .build();
    }

    private void calculateItemCosts(TechnicalSheet technicalSheet) {
        for (TechnicalSheetItem item : technicalSheet.getItems()) {
            item.calculateCost();
        }
    }

    private TechnicalSheetResponse mapToResponse(TechnicalSheet technicalSheet) {
        TechnicalSheetResponse.TechnicalSheetResponseBuilder builder = TechnicalSheetResponse.builder()
                .id(technicalSheet.getId())
                .sheetCode(technicalSheet.getSheetCode())
                .name(technicalSheet.getName())
                .description(technicalSheet.getDescription())
                .version(technicalSheet.getVersion())
                .outputQuantity(technicalSheet.getOutputQuantity())
                .outputUnit(technicalSheet.getOutputUnit())
                .preparationTime(technicalSheet.getPreparationTime())
                .cookingTime(technicalSheet.getCookingTime())
                .difficulty(technicalSheet.getDifficulty())
                .instructions(technicalSheet.getInstructions())
                .totalCost(technicalSheet.getTotalCost())
                .costPerUnit(technicalSheet.getCostPerUnit())
                .active(technicalSheet.getActive())
                .validated(technicalSheet.getValidated())
                .validatedAt(technicalSheet.getValidatedAt())
                .validatedBy(technicalSheet.getValidatedBy())
                .notes(technicalSheet.getNotes())
                .createdAt(technicalSheet.getCreatedAt())
                .updatedAt(technicalSheet.getUpdatedAt())
                .createdBy(technicalSheet.getCreatedBy())
                .updatedBy(technicalSheet.getUpdatedBy());

        // Set product info if available
        if (technicalSheet.getProduct() != null) {
            builder.productId(technicalSheet.getProduct().getId())
                    .productCode(technicalSheet.getProduct().getProductCode())
                    .productName(technicalSheet.getProduct().getName());
        }

        // Map items
        if (technicalSheet.getItems() != null && !technicalSheet.getItems().isEmpty()) {
            List<TechnicalSheetItemResponse> itemResponses = technicalSheet.getItems().stream()
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());
            builder.items(itemResponses);
        }

        TechnicalSheetResponse response = builder.build();

        // Calculate computed fields
        if (technicalSheet.getItems() != null) {
            response.setTotalItems(technicalSheet.getItems().size());
        }

        if (technicalSheet.getPreparationTime() != null && technicalSheet.getCookingTime() != null) {
            response.setTotalTime(technicalSheet.getPreparationTime() + technicalSheet.getCookingTime());
        } else if (technicalSheet.getPreparationTime() != null) {
            response.setTotalTime(technicalSheet.getPreparationTime());
        } else if (technicalSheet.getCookingTime() != null) {
            response.setTotalTime(technicalSheet.getCookingTime());
        }

        return response;
    }

    private TechnicalSheetItemResponse mapItemToResponse(TechnicalSheetItem item) {
        TechnicalSheetItemResponse.TechnicalSheetItemResponseBuilder builder = TechnicalSheetItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .cost(item.getCost())
                .displayOrder(item.getDisplayOrder())
                .notes(item.getNotes())
                .optional(item.getOptional())
                .conversionFactor(item.getConversionFactor());

        // Set raw material info if available
        if (item.getRawMaterial() != null) {
            builder.rawMaterialId(item.getRawMaterial().getId())
                    .rawMaterialCode(item.getRawMaterial().getMaterialCode())
                    .rawMaterialName(item.getRawMaterial().getMaterialName())
                    .rawMaterialUnit(item.getRawMaterial().getUnit())
                    .rawMaterialPurchasePrice(item.getRawMaterial().getPurchasePrice())
                    .rawMaterialCategory(item.getRawMaterial().getMaterialCategory());
        }

        return builder.build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
