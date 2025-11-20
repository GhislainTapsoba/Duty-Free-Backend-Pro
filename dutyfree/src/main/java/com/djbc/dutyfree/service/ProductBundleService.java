package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.BundleItemRequest;
import com.djbc.dutyfree.domain.dto.request.ProductBundleRequest;
import com.djbc.dutyfree.domain.dto.response.BundleItemResponse;
import com.djbc.dutyfree.domain.dto.response.ProductBundleResponse;
import com.djbc.dutyfree.domain.entity.*;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductBundleService {

    private final ProductBundleRepository bundleRepository;
    private final BundleItemRepository bundleItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService;

    @Transactional
    @CacheEvict(value = "bundles", allEntries = true)
    public ProductBundleResponse createBundle(ProductBundleRequest request) {
        log.info("Creating new product bundle with code: {}", request.getBundleCode());

        // Vérifier l'unicité du code
        if (bundleRepository.existsByBundleCode(request.getBundleCode())) {
            throw new BadRequestException("Bundle code already exists: " + request.getBundleCode());
        }

        // Vérifier que le bundle a au moins un produit
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Bundle must contain at least one product");
        }

        ProductBundle bundle = new ProductBundle();
        mapRequestToEntity(request, bundle);

        // Ajouter les items au bundle
        for (BundleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            BundleItem bundleItem = BundleItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .optional(itemRequest.getOptional())
                    .displayOrder(itemRequest.getDisplayOrder())
                    .notes(itemRequest.getNotes())
                    .substitutable(itemRequest.getSubstitutable())
                    .substitutionGroup(itemRequest.getSubstitutionGroup())
                    .build();

            bundle.addItem(bundleItem);
        }

        bundle = bundleRepository.save(bundle);
        log.info("Product bundle created successfully with ID: {}", bundle.getId());

        return mapEntityToResponse(bundle);
    }

    @Transactional
    @CacheEvict(value = "bundles", allEntries = true)
    public ProductBundleResponse updateBundle(Long id, ProductBundleRequest request) {
        log.info("Updating product bundle with ID: {}", id);

        ProductBundle bundle = bundleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", id));

        // Vérifier l'unicité du code si modifié
        if (!bundle.getBundleCode().equals(request.getBundleCode())) {
            if (bundleRepository.existsByBundleCode(request.getBundleCode())) {
                throw new BadRequestException("Bundle code already exists: " + request.getBundleCode());
            }
        }

        mapRequestToEntity(request, bundle);

        // Supprimer les anciens items
        bundle.getItems().clear();

        // Ajouter les nouveaux items
        for (BundleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            BundleItem bundleItem = BundleItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .optional(itemRequest.getOptional())
                    .displayOrder(itemRequest.getDisplayOrder())
                    .notes(itemRequest.getNotes())
                    .substitutable(itemRequest.getSubstitutable())
                    .substitutionGroup(itemRequest.getSubstitutionGroup())
                    .build();

            bundle.addItem(bundleItem);
        }

        bundle = bundleRepository.save(bundle);
        log.info("Product bundle updated successfully");

        return mapEntityToResponse(bundle);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "bundles", key = "#id")
    public ProductBundleResponse getBundleById(Long id) {
        log.info("Fetching product bundle by ID: {}", id);
        ProductBundle bundle = bundleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", id));
        return mapEntityToResponse(bundle);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "bundles", key = "'code-' + #bundleCode")
    public ProductBundleResponse getBundleByCode(String bundleCode) {
        log.info("Fetching product bundle by code: {}", bundleCode);
        ProductBundle bundle = bundleRepository.findByBundleCodeWithItems(bundleCode)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "bundleCode", bundleCode));
        return mapEntityToResponse(bundle);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "bundles", key = "'all'")
    public List<ProductBundleResponse> getAllBundles() {
        log.info("Fetching all product bundles");
        return bundleRepository.findAllActiveWithItems().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductBundleResponse> getActiveBundles() {
        log.info("Fetching active product bundles");
        LocalDateTime now = LocalDateTime.now();
        return bundleRepository.findActiveBundles(now).stream()
                .map(this::mapEntityToResponse)
                .filter(response -> response.getIsValidNow())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductBundleResponse> getBundlesByCategory(Long categoryId) {
        log.info("Fetching bundles by category ID: {}", categoryId);
        return bundleRepository.findByCategoryId(categoryId).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductBundleResponse> getBundlesByType(String bundleType) {
        log.info("Fetching bundles by type: {}", bundleType);
        return bundleRepository.findByBundleType(bundleType).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "bundles", allEntries = true)
    public void deleteBundle(Long id) {
        log.info("Deleting product bundle with ID: {}", id);
        ProductBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", id));
        bundleRepository.delete(bundle);
        log.info("Product bundle deleted successfully");
    }

    @Transactional
    @CacheEvict(value = "bundles", allEntries = true)
    public void activateBundle(Long id) {
        log.info("Activating bundle with ID: {}", id);
        ProductBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", id));
        bundle.setActive(true);
        bundleRepository.save(bundle);
    }

    @Transactional
    @CacheEvict(value = "bundles", allEntries = true)
    public void deactivateBundle(Long id) {
        log.info("Deactivating bundle with ID: {}", id);
        ProductBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", id));
        bundle.setActive(false);
        bundleRepository.save(bundle);
    }

    @Transactional
    public void incrementSoldCount(Long bundleId) {
        ProductBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", bundleId));
        bundle.setTodaySoldCount(bundle.getTodaySoldCount() + 1);
        bundleRepository.save(bundle);
    }

    @Transactional
    public void resetDailyCounts() {
        log.info("Resetting daily sold counts for all bundles");
        List<ProductBundle> bundles = bundleRepository.findAll();
        bundles.forEach(bundle -> bundle.setTodaySoldCount(0));
        bundleRepository.saveAll(bundles);
    }

    /**
     * Vérifie si tous les produits du bundle ont suffisamment de stock
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableStock(Long bundleId) {
        ProductBundle bundle = bundleRepository.findByIdWithItems(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", bundleId));

        for (BundleItem item : bundle.getItems()) {
            if (!item.getOptional()) { // Vérifier seulement les items non optionnels
                Integer available = stockService.getAvailableStock(item.getProduct().getId());
                if (available < item.getQuantity()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Réserve le stock pour tous les produits du bundle
     */
    @Transactional
    public void reserveBundleStock(Long bundleId, List<Long> excludedProductIds) {
        ProductBundle bundle = bundleRepository.findByIdWithItems(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBundle", "id", bundleId));

        for (BundleItem item : bundle.getItems()) {
            if (!item.getOptional() && (excludedProductIds == null || !excludedProductIds.contains(item.getProduct().getId()))) {
                stockService.reserveStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }

    private void mapRequestToEntity(ProductBundleRequest request, ProductBundle bundle) {
        bundle.setBundleCode(request.getBundleCode());
        bundle.setNameFr(request.getNameFr());
        bundle.setNameEn(request.getNameEn());
        bundle.setDescriptionFr(request.getDescriptionFr());
        bundle.setDescriptionEn(request.getDescriptionEn());
        bundle.setBundlePriceXOF(request.getBundlePriceXOF());
        bundle.setBundlePriceEUR(request.getBundlePriceEUR());
        bundle.setBundlePriceUSD(request.getBundlePriceUSD());
        bundle.setDiscountPercentage(request.getDiscountPercentage());
        bundle.setBundleType(request.getBundleType());
        bundle.setImageUrl(request.getImageUrl());
        bundle.setValidFrom(request.getValidFrom());
        bundle.setValidUntil(request.getValidUntil());
        bundle.setActive(request.getActive());
        bundle.setTimeRestricted(request.getTimeRestricted());
        bundle.setStartTime(request.getStartTime());
        bundle.setEndTime(request.getEndTime());
        bundle.setDailyLimit(request.getDailyLimit());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            bundle.setCategory(category);
        }
    }

    private ProductBundleResponse mapEntityToResponse(ProductBundle bundle) {
        List<BundleItemResponse> itemResponses = new ArrayList<>();
        if (bundle.getItems() != null) {
            itemResponses = bundle.getItems().stream()
                    .map(this::mapBundleItemToResponse)
                    .collect(Collectors.toList());
        }

        BigDecimal separatePrice = bundle.calculateSeparatePrice("XOF");
        BigDecimal bundlePrice = bundle.getBundlePrice("XOF");
        BigDecimal savings = separatePrice.subtract(bundlePrice);

        return ProductBundleResponse.builder()
                .id(bundle.getId())
                .bundleCode(bundle.getBundleCode())
                .nameFr(bundle.getNameFr())
                .nameEn(bundle.getNameEn())
                .descriptionFr(bundle.getDescriptionFr())
                .descriptionEn(bundle.getDescriptionEn())
                .categoryId(bundle.getCategory() != null ? bundle.getCategory().getId() : null)
                .categoryName(bundle.getCategory() != null ? bundle.getCategory().getName() : null)
                .bundlePriceXOF(bundle.getBundlePriceXOF())
                .bundlePriceEUR(bundle.getBundlePriceEUR())
                .bundlePriceUSD(bundle.getBundlePriceUSD())
                .discountPercentage(bundle.getDiscountPercentage())
                .bundleType(bundle.getBundleType())
                .imageUrl(bundle.getImageUrl())
                .validFrom(bundle.getValidFrom())
                .validUntil(bundle.getValidUntil())
                .active(bundle.getActive())
                .timeRestricted(bundle.getTimeRestricted())
                .startTime(bundle.getStartTime())
                .endTime(bundle.getEndTime())
                .dailyLimit(bundle.getDailyLimit())
                .todaySoldCount(bundle.getTodaySoldCount())
                .isValidNow(bundle.isValidNow())
                .separatePriceXOF(separatePrice)
                .savingsXOF(savings)
                .items(itemResponses)
                .createdAt(bundle.getCreatedAt())
                .updatedAt(bundle.getUpdatedAt())
                .build();
    }

    private BundleItemResponse mapBundleItemToResponse(BundleItem item) {
        return BundleItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productNameFr(item.getProduct().getNameFr())
                .productNameEn(item.getProduct().getNameEn())
                .productSku(item.getProduct().getSku())
                .productBarcode(item.getProduct().getBarcode())
                .quantity(item.getQuantity())
                .optional(item.getOptional())
                .displayOrder(item.getDisplayOrder())
                .notes(item.getNotes())
                .substitutable(item.getSubstitutable())
                .substitutionGroup(item.getSubstitutionGroup())
                .build();
    }
}
