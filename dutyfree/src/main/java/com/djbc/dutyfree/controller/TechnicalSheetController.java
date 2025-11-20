package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.TechnicalSheetRequest;
import com.djbc.dutyfree.domain.dto.response.TechnicalSheetResponse;
import com.djbc.dutyfree.service.TechnicalSheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technical-sheets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TechnicalSheetController {

    private final TechnicalSheetService technicalSheetService;

    /**
     * Create a new technical sheet
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> createTechnicalSheet(@Valid @RequestBody TechnicalSheetRequest request) {
        log.info("POST /api/technical-sheets - Creating technical sheet: {}", request.getSheetCode());
        TechnicalSheetResponse response = technicalSheetService.createTechnicalSheet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a technical sheet
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> updateTechnicalSheet(
            @PathVariable Long id,
            @Valid @RequestBody TechnicalSheetRequest request) {
        log.info("PUT /api/technical-sheets/{} - Updating technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.updateTechnicalSheet(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get technical sheet by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<TechnicalSheetResponse> getTechnicalSheetById(@PathVariable Long id) {
        log.info("GET /api/technical-sheets/{} - Fetching technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.getTechnicalSheetById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get technical sheet by code
     */
    @GetMapping("/code/{sheetCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<TechnicalSheetResponse> getTechnicalSheetByCode(@PathVariable String sheetCode) {
        log.info("GET /api/technical-sheets/code/{} - Fetching technical sheet by code", sheetCode);
        TechnicalSheetResponse response = technicalSheetService.getTechnicalSheetByCode(sheetCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Get technical sheet by product ID
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<TechnicalSheetResponse> getTechnicalSheetByProductId(@PathVariable Long productId) {
        log.info("GET /api/technical-sheets/product/{} - Fetching technical sheet by product", productId);
        TechnicalSheetResponse response = technicalSheetService.getTechnicalSheetByProductId(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all technical sheets
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<TechnicalSheetResponse>> getAllTechnicalSheets() {
        log.info("GET /api/technical-sheets - Fetching all technical sheets");
        List<TechnicalSheetResponse> responses = technicalSheetService.getAllTechnicalSheets();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active technical sheets
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<TechnicalSheetResponse>> getActiveTechnicalSheets() {
        log.info("GET /api/technical-sheets/active - Fetching active technical sheets");
        List<TechnicalSheetResponse> responses = technicalSheetService.getActiveTechnicalSheets();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get validated technical sheets
     */
    @GetMapping("/validated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<TechnicalSheetResponse>> getValidatedTechnicalSheets() {
        log.info("GET /api/technical-sheets/validated - Fetching validated technical sheets");
        List<TechnicalSheetResponse> responses = technicalSheetService.getValidatedTechnicalSheets();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get draft (unvalidated) technical sheets
     */
    @GetMapping("/drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TechnicalSheetResponse>> getDraftTechnicalSheets() {
        log.info("GET /api/technical-sheets/drafts - Fetching draft technical sheets");
        List<TechnicalSheetResponse> responses = technicalSheetService.getDraftTechnicalSheets();
        return ResponseEntity.ok(responses);
    }

    /**
     * Validate a technical sheet
     */
    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> validateTechnicalSheet(@PathVariable Long id) {
        log.info("PATCH /api/technical-sheets/{}/validate - Validating technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.validateTechnicalSheet(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Unvalidate a technical sheet
     */
    @PatchMapping("/{id}/unvalidate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> unvalidateTechnicalSheet(@PathVariable Long id) {
        log.info("PATCH /api/technical-sheets/{}/unvalidate - Unvalidating technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.unvalidateTechnicalSheet(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a technical sheet
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> activateTechnicalSheet(@PathVariable Long id) {
        log.info("PATCH /api/technical-sheets/{}/activate - Activating technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.activateTechnicalSheet(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a technical sheet
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> deactivateTechnicalSheet(@PathVariable Long id) {
        log.info("PATCH /api/technical-sheets/{}/deactivate - Deactivating technical sheet", id);
        TechnicalSheetResponse response = technicalSheetService.deactivateTechnicalSheet(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Recalculate costs for a technical sheet
     */
    @PostMapping("/{id}/recalculate-costs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TechnicalSheetResponse> recalculateCosts(@PathVariable Long id) {
        log.info("POST /api/technical-sheets/{}/recalculate-costs - Recalculating costs", id);
        TechnicalSheetResponse response = technicalSheetService.recalculateCosts(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a technical sheet (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTechnicalSheet(@PathVariable Long id) {
        log.info("DELETE /api/technical-sheets/{} - Deleting technical sheet", id);
        technicalSheetService.deleteTechnicalSheet(id);
        return ResponseEntity.noContent().build();
    }
}
