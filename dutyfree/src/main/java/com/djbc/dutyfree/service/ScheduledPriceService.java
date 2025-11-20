package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.ScheduledPriceRequest;
import com.djbc.dutyfree.domain.dto.response.ScheduledPriceResponse;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.ScheduledPrice;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.ScheduledPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduledPriceService {

    private final ScheduledPriceRepository scheduledPriceRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new scheduled price
     */
    @CacheEvict(value = {"scheduledPrices", "products"}, allEntries = true)
    public ScheduledPriceResponse createScheduledPrice(ScheduledPriceRequest request) {
        log.info("Creating scheduled price: {}", request.getName());

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        ScheduledPrice scheduledPrice = mapToEntity(request, product);
        scheduledPrice.setCreatedBy(getCurrentUsername());
        scheduledPrice.setUpdatedBy(getCurrentUsername());

        ScheduledPrice saved = scheduledPriceRepository.save(scheduledPrice);

        log.info("Scheduled price created: {} for product {}", saved.getId(), saved.getProduct().getName());
        return mapToResponse(saved);
    }

    /**
     * Update a scheduled price
     */
    @CacheEvict(value = {"scheduledPrices", "products"}, allEntries = true)
    public ScheduledPriceResponse updateScheduledPrice(Long id, ScheduledPriceRequest request) {
        log.info("Updating scheduled price: {}", id);

        ScheduledPrice scheduledPrice = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled price not found with id: " + id));

        // Validate product if changed
        if (!scheduledPrice.getProduct().getId().equals(request.getProductId())) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            scheduledPrice.setProduct(product);
        }

        updateEntityFromRequest(scheduledPrice, request);
        scheduledPrice.setUpdatedBy(getCurrentUsername());

        ScheduledPrice updated = scheduledPriceRepository.save(scheduledPrice);

        log.info("Scheduled price updated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Get scheduled price by ID
     */
    @Cacheable(value = "scheduledPrices", key = "#id")
    @Transactional(readOnly = true)
    public ScheduledPriceResponse getScheduledPriceById(Long id) {
        log.debug("Fetching scheduled price by ID: {}", id);

        ScheduledPrice scheduledPrice = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled price not found with id: " + id));

        return mapToResponse(scheduledPrice);
    }

    /**
     * Get all scheduled prices
     */
    @Cacheable(value = "scheduledPrices", key = "'all'")
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getAllScheduledPrices() {
        log.debug("Fetching all scheduled prices");

        return scheduledPriceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active scheduled prices
     */
    @Cacheable(value = "scheduledPrices", key = "'active'")
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getAllActiveScheduledPrices() {
        log.debug("Fetching all active scheduled prices");

        return scheduledPriceRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get scheduled prices for a product
     */
    @Cacheable(value = "scheduledPrices", key = "'product_' + #productId")
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getScheduledPricesByProduct(Long productId) {
        log.debug("Fetching scheduled prices for product: {}", productId);

        return scheduledPriceRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active scheduled prices for a product
     */
    @Cacheable(value = "scheduledPrices", key = "'product_active_' + #productId")
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getActiveScheduledPricesByProduct(Long productId) {
        log.debug("Fetching active scheduled prices for product: {}", productId);

        return scheduledPriceRepository.findActiveByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get currently valid prices for a product
     */
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getCurrentlyValidPrices(Long productId) {
        log.debug("Fetching currently valid prices for product: {}", productId);

        LocalDate now = LocalDate.now();
        List<ScheduledPrice> prices = scheduledPriceRepository.findCurrentlyValidForProduct(productId, now);

        // Filter by time and day of week
        return prices.stream()
                .filter(ScheduledPrice::isCurrentlyValid)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calculate effective price for a product (considering scheduled prices)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateEffectivePrice(Long productId, BigDecimal basePrice) {
        log.debug("Calculating effective price for product: {}", productId);

        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }

        List<ScheduledPrice> validPrices = scheduledPriceRepository
                .findCurrentlyValidForProduct(productId, LocalDate.now()).stream()
                .filter(ScheduledPrice::isCurrentlyValid)
                .collect(Collectors.toList());

        if (validPrices.isEmpty()) {
            return basePrice;
        }

        // Get the highest priority price
        ScheduledPrice highestPriority = validPrices.get(0); // Already sorted by priority DESC
        BigDecimal effectivePrice = highestPriority.calculatePrice(basePrice);

        log.debug("Effective price for product {}: {} (base: {}, rule: {})",
                productId, effectivePrice, basePrice, highestPriority.getName());

        return effectivePrice;
    }

    /**
     * Get scheduled prices by period type
     */
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getScheduledPricesByPeriodType(String periodType) {
        log.debug("Fetching scheduled prices by period type: {}", periodType);

        return scheduledPriceRepository.findByPeriodType(periodType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get scheduled prices valid in date range
     */
    @Transactional(readOnly = true)
    public List<ScheduledPriceResponse> getScheduledPricesInDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching scheduled prices valid between {} and {}", startDate, endDate);

        return scheduledPriceRepository.findValidInDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Activate a scheduled price
     */
    @CacheEvict(value = {"scheduledPrices", "products"}, allEntries = true)
    public ScheduledPriceResponse activateScheduledPrice(Long id) {
        log.info("Activating scheduled price: {}", id);

        ScheduledPrice scheduledPrice = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled price not found with id: " + id));

        scheduledPrice.setActive(true);
        scheduledPrice.setUpdatedBy(getCurrentUsername());

        ScheduledPrice updated = scheduledPriceRepository.save(scheduledPrice);

        log.info("Scheduled price activated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Deactivate a scheduled price
     */
    @CacheEvict(value = {"scheduledPrices", "products"}, allEntries = true)
    public ScheduledPriceResponse deactivateScheduledPrice(Long id) {
        log.info("Deactivating scheduled price: {}", id);

        ScheduledPrice scheduledPrice = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled price not found with id: " + id));

        scheduledPrice.setActive(false);
        scheduledPrice.setUpdatedBy(getCurrentUsername());

        ScheduledPrice updated = scheduledPriceRepository.save(scheduledPrice);

        log.info("Scheduled price deactivated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Delete a scheduled price (soft delete)
     */
    @CacheEvict(value = {"scheduledPrices", "products"}, allEntries = true)
    public void deleteScheduledPrice(Long id) {
        log.info("Deleting scheduled price: {}", id);

        ScheduledPrice scheduledPrice = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled price not found with id: " + id));

        scheduledPrice.setDeleted(true);
        scheduledPrice.setActive(false);
        scheduledPriceRepository.save(scheduledPrice);

        log.info("Scheduled price deleted: {}", id);
    }

    // Helper methods

    private ScheduledPrice mapToEntity(ScheduledPriceRequest request, Product product) {
        return ScheduledPrice.builder()
                .name(request.getName())
                .description(request.getDescription())
                .product(product)
                .priceType(request.getPriceType())
                .amount(request.getAmount())
                .percentage(request.getPercentage())
                .currency(request.getCurrency())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .timeFrom(request.getTimeFrom())
                .timeUntil(request.getTimeUntil())
                .daysOfWeek(request.getDaysOfWeek())
                .priority(request.getPriority())
                .periodType(request.getPeriodType())
                .active(request.getActive())
                .notes(request.getNotes())
                .build();
    }

    private void updateEntityFromRequest(ScheduledPrice scheduledPrice, ScheduledPriceRequest request) {
        scheduledPrice.setName(request.getName());
        scheduledPrice.setDescription(request.getDescription());
        scheduledPrice.setPriceType(request.getPriceType());
        scheduledPrice.setAmount(request.getAmount());
        scheduledPrice.setPercentage(request.getPercentage());
        scheduledPrice.setCurrency(request.getCurrency());
        scheduledPrice.setValidFrom(request.getValidFrom());
        scheduledPrice.setValidUntil(request.getValidUntil());
        scheduledPrice.setTimeFrom(request.getTimeFrom());
        scheduledPrice.setTimeUntil(request.getTimeUntil());
        scheduledPrice.setDaysOfWeek(request.getDaysOfWeek());
        scheduledPrice.setPriority(request.getPriority());
        scheduledPrice.setPeriodType(request.getPeriodType());
        scheduledPrice.setActive(request.getActive());
        scheduledPrice.setNotes(request.getNotes());
    }

    private ScheduledPriceResponse mapToResponse(ScheduledPrice scheduledPrice) {
        ScheduledPriceResponse.ScheduledPriceResponseBuilder builder = ScheduledPriceResponse.builder()
                .id(scheduledPrice.getId())
                .name(scheduledPrice.getName())
                .description(scheduledPrice.getDescription())
                .priceType(scheduledPrice.getPriceType())
                .amount(scheduledPrice.getAmount())
                .percentage(scheduledPrice.getPercentage())
                .currency(scheduledPrice.getCurrency())
                .validFrom(scheduledPrice.getValidFrom())
                .validUntil(scheduledPrice.getValidUntil())
                .timeFrom(scheduledPrice.getTimeFrom())
                .timeUntil(scheduledPrice.getTimeUntil())
                .daysOfWeek(scheduledPrice.getDaysOfWeek())
                .priority(scheduledPrice.getPriority())
                .periodType(scheduledPrice.getPeriodType())
                .active(scheduledPrice.getActive())
                .notes(scheduledPrice.getNotes())
                .createdAt(scheduledPrice.getCreatedAt())
                .updatedAt(scheduledPrice.getUpdatedAt())
                .createdBy(scheduledPrice.getCreatedBy())
                .updatedBy(scheduledPrice.getUpdatedBy());

        // Set product info
        if (scheduledPrice.getProduct() != null) {
            Product product = scheduledPrice.getProduct();
            builder.productId(product.getId())
                    .productCode(product.getProductCode())
                    .productName(product.getName());

            // Calculate effective price if base price is available
            if (product.getPriceXOF() != null) {
                BigDecimal effectivePrice = scheduledPrice.calculatePrice(product.getPriceXOF());
                builder.effectivePrice(effectivePrice);
            }
        }

        // Set currently valid status
        builder.currentlyValid(scheduledPrice.isCurrentlyValid());

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
