package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.SaleRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.SaleResponse;
import com.djbc.dutyfree.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Sales", description = "Sales management APIs")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create sale", description = "Create a new sale transaction")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(@Valid @RequestBody SaleRequest request) {
        SaleResponse sale = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale created successfully", sale));
    }

    @PostMapping("/{saleId}/complete")
    @Operation(summary = "Complete sale", description = "Mark sale as completed")
    public ResponseEntity<ApiResponse<SaleResponse>> completeSale(@PathVariable Long saleId) {
        SaleResponse sale = saleService.completeSale(saleId);
        return ResponseEntity.ok(ApiResponse.success("Sale completed successfully", sale));
    }

    @PostMapping("/{saleId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Cancel sale", description = "Cancel a sale transaction")
    public ResponseEntity<ApiResponse<Void>> cancelSale(
            @PathVariable Long saleId,
            @RequestParam String reason) {
        saleService.cancelSale(saleId, reason);
        return ResponseEntity.ok(ApiResponse.success("Sale cancelled successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Get sale details by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable Long id) {
        SaleResponse sale = saleService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    @GetMapping("/number/{saleNumber}")
    @Operation(summary = "Get sale by number", description = "Get sale details by sale number")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleBySaleNumber(@PathVariable String saleNumber) {
        SaleResponse sale = saleService.getSaleBySaleNumber(saleNumber);
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get sales by date range", description = "Get sales within a date range")
    public ResponseEntity<ApiResponse<Page<SaleResponse>>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<SaleResponse> sales = saleService.getSalesByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    @GetMapping("/cashier/{cashierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get sales by cashier", description = "Get sales made by a specific cashier")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByCashier(
            @PathVariable Long cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SaleResponse> sales = saleService.getSalesByCashier(cashierId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    @GetMapping("/cash-register/{cashRegisterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get sales by cash register", description = "Get sales from a specific cash register")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByCashRegister(
            @PathVariable Long cashRegisterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SaleResponse> sales = saleService.getSalesByCashRegister(cashRegisterId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(sales));
    }
}