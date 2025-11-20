package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.Supplier;
import com.djbc.dutyfree.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Suppliers", description = "Supplier management APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Get all suppliers")
    public ResponseEntity<ApiResponse<List<Supplier>>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active suppliers", description = "Get all active suppliers")
    public ResponseEntity<ApiResponse<List<Supplier>>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Get supplier details by ID")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get supplier by code", description = "Get supplier by code")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierByCode(@PathVariable String code) {
        Supplier supplier = supplierService.getSupplierByCode(code);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Search suppliers by name, code, or contact person")
    public ResponseEntity<ApiResponse<List<Supplier>>> searchSuppliers(@RequestParam String query) {
        List<Supplier> suppliers = supplierService.searchSuppliers(query);
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @PostMapping
    @Operation(summary = "Create supplier", description = "Create a new supplier")
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(@RequestBody Supplier supplier) {
        Supplier createdSupplier = supplierService.createSupplier(supplier);
        return ResponseEntity.status(201).body(ApiResponse.success("Supplier created successfully", createdSupplier));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Update an existing supplier")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier) {
        Supplier updatedSupplier = supplierService.updateSupplier(id, supplier);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", updatedSupplier));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Delete a supplier (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully", null));
    }
}