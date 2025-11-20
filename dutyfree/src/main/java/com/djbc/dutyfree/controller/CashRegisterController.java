package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.CashRegisterRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.CashRegisterResponse;
import com.djbc.dutyfree.domain.entity.CashRegister;
import com.djbc.dutyfree.service.CashRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    // ===========================
    // CREATE
    // ===========================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create cash register", description = "Create a new cash register")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> createCashRegister(
            @RequestBody CashRegisterRequest request) {

        CashRegister cashRegister = cashRegisterService.createCashRegister(request);
        CashRegisterResponse response = cashRegisterService.toResponse(cashRegister);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cash register created successfully", response));
    }

    // ===========================
    // OPEN
    // ===========================
    @PostMapping("/{cashRegisterId}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Open cash register", description = "Open a cash register for business")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> openCashRegister(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal openingBalance) {

        CashRegister cashRegister = cashRegisterService.openCashRegister(cashRegisterId, openingBalance);
        return ResponseEntity.ok(ApiResponse.success(
                "Cash register opened successfully",
                cashRegisterService.toResponse(cashRegister)
        ));
    }

    // ===========================
    // CLOSE
    // ===========================
    @PostMapping("/{cashRegisterId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Close cash register", description = "Close a cash register")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> closeCashRegister(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal closingBalance) {

        CashRegister cashRegister = cashRegisterService.closeCashRegister(cashRegisterId, closingBalance);
        return ResponseEntity.ok(ApiResponse.success(
                "Cash register closed successfully",
                cashRegisterService.toResponse(cashRegister)
        ));
    }

    // ===========================
    // ADD CASH
    // ===========================
    @PostMapping("/{cashRegisterId}/add-cash")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Add cash", description = "Add cash to register drawer")
    public ResponseEntity<ApiResponse<Void>> addCash(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal amount) {

        cashRegisterService.addCash(cashRegisterId, amount);
        return ResponseEntity.ok(ApiResponse.success("Cash added successfully", null));
    }

    // ===========================
    // REMOVE CASH
    // ===========================
    @PostMapping("/{cashRegisterId}/remove-cash")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Remove cash", description = "Remove cash from register drawer")
    public ResponseEntity<ApiResponse<Void>> removeCash(
            @PathVariable Long cashRegisterId,
            @RequestParam BigDecimal amount) {

        cashRegisterService.removeCash(cashRegisterId, amount);
        return ResponseEntity.ok(ApiResponse.success("Cash removed successfully", null));
    }

    // ===========================
    // GET BY ID
    // ===========================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Get cash register by ID", description = "Get cash register details by ID")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> getCashRegisterById(@PathVariable Long id) {
        CashRegister cashRegister = cashRegisterService.getCashRegisterById(id);
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.toResponse(cashRegister)));
    }

    // ===========================
    // GET BY NUMBER
    // ===========================
    @GetMapping("/number/{registerNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'CAISSIER')")
    @Operation(summary = "Get cash register by number", description = "Get cash register details by number")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> getCashRegisterByNumber(@PathVariable String registerNumber) {
        CashRegister cashRegister = cashRegisterService.getCashRegisterByNumber(registerNumber);
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.toResponse(cashRegister)));
    }

    // ===========================
    // GET ALL
    // ===========================
    @GetMapping
    @Operation(summary = "Get all cash registers", description = "Get all cash registers")
    public ResponseEntity<ApiResponse<List<CashRegisterResponse>>> getAllCashRegisters() {
        try {
            List<CashRegisterResponse> responses = cashRegisterService.getAllCashRegisters()
                    .stream()
                    .map(cashRegisterService::toResponse)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("Error fetching cash registers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load cash registers"));
        }
    }

    // ===========================
    // GET OPEN CASH REGISTERS
    // ===========================
    @GetMapping("/open")
    @Operation(summary = "Get open cash registers", description = "Get all currently open cash registers")
    public ResponseEntity<ApiResponse<List<CashRegisterResponse>>> getOpenCashRegisters() {
        List<CashRegisterResponse> responses = cashRegisterService.getOpenCashRegisters()
                .stream()
                .map(cashRegisterService::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ===========================
    // DEACTIVATE
    // ===========================
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate cash register", description = "Deactivate a cash register")
    public ResponseEntity<ApiResponse<Void>> deactivateCashRegister(@PathVariable Long id) {
        cashRegisterService.deactivateCashRegister(id);
        return ResponseEntity.ok(ApiResponse.success("Cash register deactivated successfully", null));
    }
}
