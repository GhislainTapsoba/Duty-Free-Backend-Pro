package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.ScheduledPriceRequest;
import com.djbc.dutyfree.domain.dto.response.ScheduledPriceResponse;
import com.djbc.dutyfree.service.ScheduledPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduled-prices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduledPriceController {

    private final ScheduledPriceService scheduledPriceService;

    /**
     * Create a new scheduled price
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ScheduledPriceResponse> createScheduledPrice(
            @Valid @RequestBody ScheduledPriceRequest request) {
        log.info("POST /api/scheduled-prices - Creating scheduled price: {}", request.getName());
        ScheduledPriceResponse response = scheduledPriceService.createScheduledPrice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a scheduled price
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ScheduledPriceResponse> updateScheduledPrice(
            @PathVariable Long id,
            @Valid @RequestBody ScheduledPriceRequest request) {
        log.info("PUT /api/scheduled-prices/{} - Updating scheduled price", id);
        ScheduledPriceResponse response = scheduledPriceService.updateScheduledPrice(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get scheduled price by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ScheduledPriceResponse> getScheduledPriceById(@PathVariable Long id) {
        log.info("GET /api/scheduled-prices/{} - Fetching scheduled price", id);
        ScheduledPriceResponse response = scheduledPriceService.getScheduledPriceById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all scheduled prices
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getAllScheduledPrices() {
        log.info("GET /api/scheduled-prices - Fetching all scheduled prices");
        List<ScheduledPriceResponse> responses = scheduledPriceService.getAllScheduledPrices();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all active scheduled prices
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getAllActiveScheduledPrices() {
        log.info("GET /api/scheduled-prices/active - Fetching all active scheduled prices");
        List<ScheduledPriceResponse> responses = scheduledPriceService.getAllActiveScheduledPrices();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get scheduled prices for a product
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getScheduledPricesByProduct(
            @PathVariable Long productId) {
        log.info("GET /api/scheduled-prices/product/{} - Fetching scheduled prices for product", productId);
        List<ScheduledPriceResponse> responses = scheduledPriceService.getScheduledPricesByProduct(productId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active scheduled prices for a product
     */
    @GetMapping("/product/{productId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getActiveScheduledPricesByProduct(
            @PathVariable Long productId) {
        log.info("GET /api/scheduled-prices/product/{}/active - Fetching active scheduled prices", productId);
        List<ScheduledPriceResponse> responses = scheduledPriceService.getActiveScheduledPricesByProduct(productId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get currently valid prices for a product
     */
    @GetMapping("/product/{productId}/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getCurrentlyValidPrices(
            @PathVariable Long productId) {
        log.info("GET /api/scheduled-prices/product/{}/current - Fetching currently valid prices", productId);
        List<ScheduledPriceResponse> responses = scheduledPriceService.getCurrentlyValidPrices(productId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Calculate effective price for a product
     */
    @GetMapping("/product/{productId}/effective-price")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Map<String, Object>> calculateEffectivePrice(
            @PathVariable Long productId,
            @RequestParam BigDecimal basePrice) {
        log.info("GET /api/scheduled-prices/product/{}/effective-price - Calculating effective price", productId);

        BigDecimal effectivePrice = scheduledPriceService.calculateEffectivePrice(productId, basePrice);

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("basePrice", basePrice);
        result.put("effectivePrice", effectivePrice);
        result.put("discount", basePrice.subtract(effectivePrice));
        result.put("discountPercentage",
                basePrice.compareTo(BigDecimal.ZERO) > 0
                        ? basePrice.subtract(effectivePrice)
                        .divide(basePrice, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Get scheduled prices by period type
     */
    @GetMapping("/period/{periodType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getScheduledPricesByPeriodType(
            @PathVariable String periodType) {
        log.info("GET /api/scheduled-prices/period/{} - Fetching scheduled prices by period type", periodType);
        List<ScheduledPriceResponse> responses = scheduledPriceService.getScheduledPricesByPeriodType(periodType);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get scheduled prices valid in date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ScheduledPriceResponse>> getScheduledPricesInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/scheduled-prices/date-range - Fetching scheduled prices between {} and {}", startDate, endDate);
        List<ScheduledPriceResponse> responses = scheduledPriceService.getScheduledPricesInDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Activate a scheduled price
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ScheduledPriceResponse> activateScheduledPrice(@PathVariable Long id) {
        log.info("PATCH /api/scheduled-prices/{}/activate - Activating scheduled price", id);
        ScheduledPriceResponse response = scheduledPriceService.activateScheduledPrice(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a scheduled price
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ScheduledPriceResponse> deactivateScheduledPrice(@PathVariable Long id) {
        log.info("PATCH /api/scheduled-prices/{}/deactivate - Deactivating scheduled price", id);
        ScheduledPriceResponse response = scheduledPriceService.deactivateScheduledPrice(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a scheduled price (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScheduledPrice(@PathVariable Long id) {
        log.info("DELETE /api/scheduled-prices/{} - Deleting scheduled price", id);
        scheduledPriceService.deleteScheduledPrice(id);
        return ResponseEntity.noContent().build();
    }
}
