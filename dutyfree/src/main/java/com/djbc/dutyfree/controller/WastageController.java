package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.CreateWastageRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.WastageResponse;
import com.djbc.dutyfree.service.WastageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wastages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Wastages", description = "Wastage/Loss management APIs")
public class WastageController {

    private final WastageService wastageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'STOCK_MANAGER')")
    @Operation(summary = "Create wastage record")
    public ResponseEntity<ApiResponse<WastageResponse>> create(
            @Valid @RequestBody CreateWastageRequest request,
            Authentication authentication) {
        WastageResponse wastage = wastageService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wastage record created", wastage));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'STOCK_MANAGER')")
    @Operation(summary = "Get all wastages")
    public ResponseEntity<ApiResponse<List<WastageResponse>>> getAll() {
        List<WastageResponse> wastages = wastageService.getAll();
        return ResponseEntity.ok(ApiResponse.success(wastages));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'STOCK_MANAGER')")
    @Operation(summary = "Get wastages by date range")
    public ResponseEntity<ApiResponse<List<WastageResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WastageResponse> wastages = wastageService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(wastages));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get pending wastages")
    public ResponseEntity<ApiResponse<List<WastageResponse>>> getPending() {
        List<WastageResponse> wastages = wastageService.getPending();
        return ResponseEntity.ok(ApiResponse.success(wastages));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Approve wastage")
    public ResponseEntity<ApiResponse<WastageResponse>> approve(
            @PathVariable Long id,
            Authentication authentication) {
        WastageResponse wastage = wastageService.approve(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Wastage approved", wastage));
    }

    @GetMapping("/total-value-lost")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get total value lost")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalValueLost(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = wastageService.getTotalValueLost(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
}