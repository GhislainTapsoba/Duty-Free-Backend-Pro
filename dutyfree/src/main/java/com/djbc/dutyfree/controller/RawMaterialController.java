package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.RawMaterialRequest;
import com.djbc.dutyfree.domain.dto.response.RawMaterialResponse;
import com.djbc.dutyfree.service.RawMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RawMaterialController {

    private final RawMaterialService rawMaterialService;

    /**
     * Create a new raw material
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> createRawMaterial(@Valid @RequestBody RawMaterialRequest request) {
        log.info("POST /api/raw-materials - Creating raw material: {}", request.getMaterialCode());
        RawMaterialResponse response = rawMaterialService.createRawMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a raw material
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> updateRawMaterial(
            @PathVariable Long id,
            @Valid @RequestBody RawMaterialRequest request) {
        log.info("PUT /api/raw-materials/{} - Updating raw material", id);
        RawMaterialResponse response = rawMaterialService.updateRawMaterial(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get raw material by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<RawMaterialResponse> getRawMaterialById(@PathVariable Long id) {
        log.info("GET /api/raw-materials/{} - Fetching raw material", id);
        RawMaterialResponse response = rawMaterialService.getRawMaterialById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get raw material by code
     */
    @GetMapping("/code/{materialCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<RawMaterialResponse> getRawMaterialByCode(@PathVariable String materialCode) {
        log.info("GET /api/raw-materials/code/{} - Fetching raw material by code", materialCode);
        RawMaterialResponse response = rawMaterialService.getRawMaterialByCode(materialCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all raw materials
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<RawMaterialResponse>> getAllRawMaterials() {
        log.info("GET /api/raw-materials - Fetching all raw materials");
        List<RawMaterialResponse> responses = rawMaterialService.getAllRawMaterials();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active raw materials
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<RawMaterialResponse>> getActiveRawMaterials() {
        log.info("GET /api/raw-materials/active - Fetching active raw materials");
        List<RawMaterialResponse> responses = rawMaterialService.getActiveRawMaterials();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get raw materials by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<RawMaterialResponse>> getRawMaterialsByCategory(@PathVariable String category) {
        log.info("GET /api/raw-materials/category/{} - Fetching raw materials by category", category);
        List<RawMaterialResponse> responses = rawMaterialService.getRawMaterialsByCategory(category);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get low stock raw materials
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RawMaterialResponse>> getLowStockMaterials() {
        log.info("GET /api/raw-materials/low-stock - Fetching low stock raw materials");
        List<RawMaterialResponse> responses = rawMaterialService.getLowStockMaterials();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get raw materials needing reorder
     */
    @GetMapping("/needing-reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RawMaterialResponse>> getMaterialsNeedingReorder() {
        log.info("GET /api/raw-materials/needing-reorder - Fetching raw materials needing reorder");
        List<RawMaterialResponse> responses = rawMaterialService.getMaterialsNeedingReorder();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get perishable raw materials
     */
    @GetMapping("/perishable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RawMaterialResponse>> getPerishableMaterials() {
        log.info("GET /api/raw-materials/perishable - Fetching perishable raw materials");
        List<RawMaterialResponse> responses = rawMaterialService.getPerishableMaterials();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update stock quantity
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> updateStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {
        log.info("PATCH /api/raw-materials/{}/stock - Updating stock to {}", id, quantity);
        RawMaterialResponse response = rawMaterialService.updateStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Add to stock
     */
    @PostMapping("/{id}/stock/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> addToStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {
        log.info("POST /api/raw-materials/{}/stock/add - Adding {} to stock", id, quantity);
        RawMaterialResponse response = rawMaterialService.addToStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Reduce from stock
     */
    @PostMapping("/{id}/stock/reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> reduceFromStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {
        log.info("POST /api/raw-materials/{}/stock/reduce - Reducing {} from stock", id, quantity);
        RawMaterialResponse response = rawMaterialService.reduceFromStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a raw material
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> activateRawMaterial(@PathVariable Long id) {
        log.info("PATCH /api/raw-materials/{}/activate - Activating raw material", id);
        RawMaterialResponse response = rawMaterialService.activateRawMaterial(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a raw material
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RawMaterialResponse> deactivateRawMaterial(@PathVariable Long id) {
        log.info("PATCH /api/raw-materials/{}/deactivate - Deactivating raw material", id);
        RawMaterialResponse response = rawMaterialService.deactivateRawMaterial(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a raw material (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRawMaterial(@PathVariable Long id) {
        log.info("DELETE /api/raw-materials/{} - Deleting raw material", id);
        rawMaterialService.deleteRawMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
