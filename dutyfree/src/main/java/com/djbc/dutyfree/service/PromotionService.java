package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Category;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.Promotion;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CategoryRepository;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public Promotion createPromotion(Promotion promotion) {
        // Validate code uniqueness
        if (promotionRepository.existsByCode(promotion.getCode())) {
            throw new BadRequestException("Promotion with code " + promotion.getCode() + " already exists");
        }

        // Validate dates
        if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        promotion.setActive(true);
        promotion.setUsageCount(0);
        promotion = promotionRepository.save(promotion);

        log.info("Promotion created: {}", promotion.getCode());
        return promotion;
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public Promotion updatePromotion(Long id, Promotion promotionData) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        // Validate code uniqueness (excluding current promotion)
        if (!promotion.getCode().equals(promotionData.getCode()) &&
                promotionRepository.existsByCode(promotionData.getCode())) {
            throw new BadRequestException("Promotion with code " + promotionData.getCode() + " already exists");
        }

        promotion.setCode(promotionData.getCode());
        promotion.setName(promotionData.getName());
        promotion.setDescription(promotionData.getDescription());
        promotion.setStartDate(promotionData.getStartDate());
        promotion.setEndDate(promotionData.getEndDate());
        promotion.setDiscountType(promotionData.getDiscountType());
        promotion.setDiscountValue(promotionData.getDiscountValue());
        promotion.setMinimumPurchaseAmount(promotionData.getMinimumPurchaseAmount());
        promotion.setMaximumDiscountAmount(promotionData.getMaximumDiscountAmount());
        promotion.setActive(promotionData.getActive());
        promotion.setStackable(promotionData.getStackable());
        promotion.setUsageLimit(promotionData.getUsageLimit());
        promotion.setApplyToAllProducts(promotionData.getApplyToAllProducts());
        promotion.setTerms(promotionData.getTerms());

        promotion = promotionRepository.save(promotion);
        log.info("Promotion updated: {}", promotion.getCode());

        return promotion;
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void addProductToPromotion(Long promotionId, Long productId) {
        Promotion promotion = promotionRepository.findByIdWithProducts(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!promotion.getApplicableProducts().contains(product)) {
            promotion.getApplicableProducts().add(product);
            promotionRepository.save(promotion);
            log.info("Added product {} to promotion {}", productId, promotionId);
        }
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void removeProductFromPromotion(Long promotionId, Long productId) {
        Promotion promotion = promotionRepository.findByIdWithProducts(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        promotion.getApplicableProducts().remove(product);
        promotionRepository.save(promotion);
        log.info("Removed product {} from promotion {}", productId, promotionId);
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void addCategoryToPromotion(Long promotionId, Long categoryId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (!promotion.getApplicableCategories().contains(category)) {
            promotion.getApplicableCategories().add(category);
            promotionRepository.save(promotion);
            log.info("Added category {} to promotion {}", categoryId, promotionId);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promotions")
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsForProduct(Long productId) {
        return promotionRepository.findActivePromotionsForProduct(productId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionByCode(String code) {
        return promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "code", code));
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void incrementUsageCount(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        promotion.setUsageCount(promotion.getUsageCount() + 1);

        // Check if usage limit reached
        if (promotion.getUsageLimit() != null &&
                promotion.getUsageCount() >= promotion.getUsageLimit()) {
            promotion.setActive(false);
            log.info("Promotion {} usage limit reached, deactivated", promotion.getCode());
        }

        promotionRepository.save(promotion);
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void activatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        promotion.setActive(true);
        promotionRepository.save(promotion);
        log.info("Promotion activated: {}", promotion.getCode());
    }

    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        promotion.setActive(false);
        promotionRepository.save(promotion);
        log.info("Promotion deactivated: {}", promotion.getCode());
    }

    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal amount) {
        if ("PERCENTAGE".equals(promotion.getDiscountType())) {
            BigDecimal discount = amount.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            // Apply maximum discount limit if set
            if (promotion.getMaximumDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaximumDiscountAmount()) > 0) {
                discount = promotion.getMaximumDiscountAmount();
            }

            return discount;
        } else { // FIXED_AMOUNT
            return promotion.getDiscountValue();
        }
    }
}