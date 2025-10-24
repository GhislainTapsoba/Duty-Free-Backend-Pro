package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.Stock;
import com.djbc.dutyfree.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Stock", description = "Stock management APIs")
//@PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
public class StockController {

    private final StockService stockService;

    @PostMapping
    @Operation(summary = "Add stock", description = "Add stock for a product")
    public ResponseEntity<ApiResponse<Stock>> addStock(
            @RequestParam Long productId,
            @RequestParam(required = false) Long sommierId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {

        Stock stock = stockService.addStock(productId, sommierId, quantity, location, lotNumber, expiryDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock added successfully", stock));
    }

    @PutMapping("/{stockId}/adjust")
    @Operation(summary = "Adjust stock", description = "Adjust stock quantity")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @PathVariable Long stockId,
            @RequestParam Integer newQuantity) {
        stockService.adjustStock(stockId, newQuantity);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", null));
    }

    @PostMapping("/{productId}/reserve")
    @Operation(summary = "Reserve stock", description = "Reserve stock for a sale")
    public ResponseEntity<ApiResponse<Void>> reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        stockService.reserveStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock reserved successfully", null));
    }

    @PostMapping("/{productId}/release")
    @Operation(summary = "Release stock", description = "Release reserved stock")
    public ResponseEntity<ApiResponse<Void>> releaseReservedStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        stockService.releaseReservedStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock released successfully", null));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stocks by product", description = "Get all stock entries for a product")
    public ResponseEntity<ApiResponse<List<Stock>>> getStocksByProduct(@PathVariable Long productId) {
        List<Stock> stocks = stockService.getStocksByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/product/{productId}/total")
    @Operation(summary = "Get total stock", description = "Get total stock quantity for a product")
    public ResponseEntity<ApiResponse<Integer>> getTotalStock(@PathVariable Long productId) {
        Integer total = stockService.getTotalStock(productId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/product/{productId}/available")
    @Operation(summary = "Get available stock", description = "Get available stock quantity for a product")
    public ResponseEntity<ApiResponse<Integer>> getAvailableStock(@PathVariable Long productId) {
        Integer available = stockService.getAvailableStock(productId);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring stock", description = "Get stock items expiring soon")
    public ResponseEntity<ApiResponse<List<Stock>>> getExpiringStock(@RequestParam(defaultValue = "30") int daysAhead) {
        List<Stock> stocks = stockService.getExpiringStock(daysAhead);
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired stock", description = "Get expired stock items")
    public ResponseEntity<ApiResponse<List<Stock>>> getExpiredStock() {
        List<Stock> stocks = stockService.getExpiredStock();
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    @GetMapping("/low")
    @Operation(summary = "Get low stock", description = "Retrieve products with low available quantity")
    public ResponseEntity<ApiResponse<List<Stock>>> getLowStock(
            @RequestParam(value = "threshold", defaultValue = "10") int threshold) {
        List<Stock> lowStocks = stockService.getLowStock(threshold);
        return ResponseEntity.ok(ApiResponse.success(lowStocks));
    }
}