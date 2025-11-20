package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.ProductBundleRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.ProductBundleResponse;
import com.djbc.dutyfree.service.ProductBundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Product Bundles", description = "Menu and formula management APIs")
@Slf4j
public class ProductBundleController {

    private final ProductBundleService bundleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(summary = "Create a new product bundle", description = "Create a new menu/formula")
    public ResponseEntity<ApiResponse<ProductBundleResponse>> createBundle(@RequestBody ProductBundleRequest request) {
        log.info("REST request to create product bundle: {}", request.getBundleCode());
        ProductBundleResponse response = bundleService.createBundle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bundle created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(summary = "Update a product bundle", description = "Update an existing menu/formula")
    public ResponseEntity<ApiResponse<ProductBundleResponse>> updateBundle(
            @PathVariable Long id,
            @RequestBody ProductBundleRequest request) {
        log.info("REST request to update product bundle: {}", id);
        ProductBundleResponse response = bundleService.updateBundle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bundle updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bundle by ID", description = "Get a bundle by its ID")
    public ResponseEntity<ApiResponse<ProductBundleResponse>> getBundleById(@PathVariable Long id) {
        log.info("REST request to get product bundle: {}", id);
        ProductBundleResponse response = bundleService.getBundleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{bundleCode}")
    @Operation(summary = "Get bundle by code", description = "Get a bundle by its code")
    public ResponseEntity<ApiResponse<ProductBundleResponse>> getBundleByCode(@PathVariable String bundleCode) {
        log.info("REST request to get product bundle by code: {}", bundleCode);
        ProductBundleResponse response = bundleService.getBundleByCode(bundleCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all bundles", description = "Get all product bundles")
    public ResponseEntity<ApiResponse<List<ProductBundleResponse>>> getAllBundles() {
        log.info("REST request to get all product bundles");
        List<ProductBundleResponse> responses = bundleService.getAllBundles();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active bundles", description = "Get all currently active and valid bundles")
    public ResponseEntity<ApiResponse<List<ProductBundleResponse>>> getActiveBundles() {
        log.info("REST request to get active product bundles");
        List<ProductBundleResponse> responses = bundleService.getActiveBundles();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get bundles by category", description = "Get bundles in a specific category")
    public ResponseEntity<ApiResponse<List<ProductBundleResponse>>> getBundlesByCategory(
            @PathVariable Long categoryId) {
        log.info("REST request to get bundles by category: {}", categoryId);
        List<ProductBundleResponse> responses = bundleService.getBundlesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/type/{bundleType}")
    @Operation(summary = "Get bundles by type", description = "Get bundles by type (MENU, COMBO, FORMULA)")
    public ResponseEntity<ApiResponse<List<ProductBundleResponse>>> getBundlesByType(
            @PathVariable String bundleType) {
        log.info("REST request to get bundles by type: {}", bundleType);
        List<ProductBundleResponse> responses = bundleService.getBundlesByType(bundleType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a bundle", description = "Delete a product bundle")
    public ResponseEntity<ApiResponse<Void>> deleteBundle(@PathVariable Long id) {
        log.info("REST request to delete product bundle: {}", id);
        bundleService.deleteBundle(id);
        return ResponseEntity.ok(ApiResponse.success("Bundle deleted successfully", null));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(summary = "Activate a bundle", description = "Activate a product bundle")
    public ResponseEntity<ApiResponse<Void>> activateBundle(@PathVariable Long id) {
        log.info("REST request to activate product bundle: {}", id);
        bundleService.activateBundle(id);
        return ResponseEntity.ok(ApiResponse.success("Bundle activated successfully", null));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(summary = "Deactivate a bundle", description = "Deactivate a product bundle")
    public ResponseEntity<ApiResponse<Void>> deactivateBundle(@PathVariable Long id) {
        log.info("REST request to deactivate product bundle: {}", id);
        bundleService.deactivateBundle(id);
        return ResponseEntity.ok(ApiResponse.success("Bundle deactivated successfully", null));
    }

    @GetMapping("/{id}/stock-available")
    @Operation(summary = "Check stock availability", description = "Check if bundle has sufficient stock")
    public ResponseEntity<ApiResponse<Boolean>> checkStockAvailability(@PathVariable Long id) {
        log.info("REST request to check stock availability for bundle: {}", id);
        boolean available = bundleService.hasAvailableStock(id);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    @PostMapping("/reset-daily-counts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset daily counts", description = "Reset daily sold counts for all bundles (scheduled task)")
    public ResponseEntity<ApiResponse<Void>> resetDailyCounts() {
        log.info("REST request to reset daily counts for all bundles");
        bundleService.resetDailyCounts();
        return ResponseEntity.ok(ApiResponse.success("Daily counts reset successfully", null));
    }
}
