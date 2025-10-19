package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.CashRegister;
import com.djbc.dutyfree.service.CashRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cash-registers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Cash Registers", description = "Cash register management APIs")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create cash register", description = "Create a new cash register")
    public ResponseEntity<ApiResponse<CashRegister>> createCashRegister(
            @RequestParam String registerNumber,
            @RequestParam String name,
            @RequestParam(required = false) String location) {
        CashRegister cashRegister = cashRegisterService.createCashRegister(registerNumber, name, location);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cash register created successfully", cashRegister));
    }

    @PostMapping("/{cashRegisterId}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Open cash register", description = "Open a cash register for business")
    public ResponseEntity<ApiResponse<CashRegister>> openCashRegister(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal openingBalance) {
        CashRegister cashRegister = cashRegisterService.openCashRegister(cashRegisterId, openingBalance);
        return ResponseEntity.ok(ApiResponse.success("Cash register opened successfully", cashRegister));
    }

    @PostMapping("/{cashRegisterId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Close cash register", description = "Close a cash register")
    public ResponseEntity<ApiResponse<CashRegister>> closeCashRegister(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal closingBalance) {
        CashRegister cashRegister = cashRegisterService.closeCashRegister(cashRegisterId, closingBalance);
        return ResponseEntity.ok(ApiResponse.success("Cash register closed successfully", cashRegister));
    }

    @PostMapping("/{cashRegisterId}/add-cash")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Add cash", description = "Add cash to register drawer")
    public ResponseEntity<ApiResponse<Void>> addCash(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal amount) {
        cashRegisterService.addCash(cashRegisterId, amount);
        return ResponseEntity.ok(ApiResponse.success("Cash added successfully", null));
    }

    @PostMapping("/{cashRegisterId}/remove-cash")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Remove cash", description = "Remove cash from register drawer")
    public ResponseEntity<ApiResponse<Void>> removeCash(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal amount) {
        cashRegisterService.removeCash(cashRegisterId, amount);
        return ResponseEntity.ok(ApiResponse.success("Cash removed successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cash register by ID", description = "Get cash register details by ID")
    public ResponseEntity<ApiResponse<CashRegister>> getCashRegisterById(@PathVariable Long id) {
        CashRegister cashRegister = cashRegisterService.getCashRegisterById(id);
        return ResponseEntity.ok(ApiResponse.success(cashRegister));
    }

    @GetMapping("/number/{registerNumber}")
    @Operation(summary = "Get cash register by number", description = "Get cash register by register number")
    public ResponseEntity<ApiResponse<CashRegister>> getCashRegisterByNumber(@PathVariable String registerNumber) {
        CashRegister cashRegister = cashRegisterService.getCashRegisterByNumber(registerNumber);
        return ResponseEntity.ok(ApiResponse.success(cashRegister));
    }

    @GetMapping
    @Operation(summary = "Get all cash registers", description = "Get all active cash registers")
    public ResponseEntity<ApiResponse<List<CashRegister>>> getAllCashRegisters() {
        List<CashRegister> cashRegisters = cashRegisterService.getAllCashRegisters();
        return ResponseEntity.ok(ApiResponse.success(cashRegisters));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open cash registers", description = "Get all currently open cash registers")
    public ResponseEntity<ApiResponse<List<CashRegister>>> getOpenCashRegisters() {
        List<CashRegister> cashRegisters = cashRegisterService.getOpenCashRegisters();
        return ResponseEntity.ok(ApiResponse.success(cashRegisters));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate cash register", description = "Deactivate a cash register")
    public ResponseEntity<ApiResponse<Void>> deactivateCashRegister(@PathVariable Long id) {
        cashRegisterService.deactivateCashRegister(id);
        return ResponseEntity.ok(ApiResponse.success("Cash register deactivated successfully", null));
    }
}