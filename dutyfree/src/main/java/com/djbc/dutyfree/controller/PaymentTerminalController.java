package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.PaymentTerminalRequest;
import com.djbc.dutyfree.domain.dto.request.TerminalPaymentRequest;
import com.djbc.dutyfree.domain.dto.response.PaymentTerminalResponse;
import com.djbc.dutyfree.domain.dto.response.TerminalTransactionResponse;
import com.djbc.dutyfree.domain.entity.PaymentTerminal;
import com.djbc.dutyfree.service.PaymentTerminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payment-terminals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentTerminalController {

    private final PaymentTerminalService paymentTerminalService;

    /**
     * Create a new payment terminal
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentTerminalResponse> createTerminal(
            @Valid @RequestBody PaymentTerminalRequest request) {
        log.info("POST /api/payment-terminals - Creating terminal: {}", request.getTerminalId());
        PaymentTerminalResponse response = paymentTerminalService.createTerminal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a payment terminal
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentTerminalResponse> updateTerminal(
            @PathVariable Long id,
            @Valid @RequestBody PaymentTerminalRequest request) {
        log.info("PUT /api/payment-terminals/{} - Updating terminal", id);
        PaymentTerminalResponse response = paymentTerminalService.updateTerminal(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get terminal by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<PaymentTerminalResponse> getTerminalById(@PathVariable Long id) {
        log.info("GET /api/payment-terminals/{} - Fetching terminal", id);
        PaymentTerminalResponse response = paymentTerminalService.getTerminalById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get terminal by terminal ID
     */
    @GetMapping("/terminal-id/{terminalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<PaymentTerminalResponse> getTerminalByTerminalId(@PathVariable String terminalId) {
        log.info("GET /api/payment-terminals/terminal-id/{} - Fetching terminal", terminalId);
        PaymentTerminalResponse response = paymentTerminalService.getTerminalByTerminalId(terminalId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active terminals
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PaymentTerminalResponse>> getAllActiveTerminals() {
        log.info("GET /api/payment-terminals - Fetching all active terminals");
        List<PaymentTerminalResponse> responses = paymentTerminalService.getAllActiveTerminals();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get terminals by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PaymentTerminalResponse>> getTerminalsByStatus(@PathVariable String status) {
        log.info("GET /api/payment-terminals/status/{} - Fetching terminals", status);
        PaymentTerminal.TerminalStatus terminalStatus = PaymentTerminal.TerminalStatus.valueOf(status.toUpperCase());
        List<PaymentTerminalResponse> responses = paymentTerminalService.getTerminalsByStatus(terminalStatus);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get terminals by cash register
     */
    @GetMapping("/cash-register/{cashRegisterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PaymentTerminalResponse>> getTerminalsByCashRegister(
            @PathVariable Long cashRegisterId) {
        log.info("GET /api/payment-terminals/cash-register/{} - Fetching terminals", cashRegisterId);
        List<PaymentTerminalResponse> responses = paymentTerminalService.getTerminalsByCashRegister(cashRegisterId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get available terminals for payment
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PaymentTerminalResponse>> getAvailableTerminals(
            @RequestParam(required = false) Long cashRegisterId) {
        log.info("GET /api/payment-terminals/available - Fetching available terminals");
        List<PaymentTerminalResponse> responses = paymentTerminalService.getAvailableTerminals(cashRegisterId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Process payment through terminal
     */
    @PostMapping("/process-payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<TerminalTransactionResponse> processPayment(
            @Valid @RequestBody TerminalPaymentRequest request) {
        log.info("POST /api/payment-terminals/process-payment - Processing payment");
        TerminalTransactionResponse response = paymentTerminalService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update terminal heartbeat
     */
    @PostMapping("/{terminalId}/heartbeat")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Void> updateHeartbeat(
            @PathVariable String terminalId,
            @RequestParam String status) {
        log.debug("POST /api/payment-terminals/{}/heartbeat - Updating heartbeat", terminalId);
        PaymentTerminal.TerminalStatus terminalStatus = PaymentTerminal.TerminalStatus.valueOf(status.toUpperCase());
        paymentTerminalService.updateTerminalHeartbeat(terminalId, terminalStatus);
        return ResponseEntity.ok().build();
    }

    /**
     * Deactivate a terminal
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTerminal(@PathVariable Long id) {
        log.info("POST /api/payment-terminals/{}/deactivate - Deactivating terminal", id);
        paymentTerminalService.deactivateTerminal(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a terminal (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTerminal(@PathVariable Long id) {
        log.info("DELETE /api/payment-terminals/{} - Deleting terminal", id);
        paymentTerminalService.deleteTerminal(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<TerminalTransactionResponse> getTransactionById(@PathVariable Long id) {
        log.info("GET /api/payment-terminals/transactions/{} - Fetching transaction", id);
        TerminalTransactionResponse response = paymentTerminalService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by terminal
     */
    @GetMapping("/{terminalId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<TerminalTransactionResponse>> getTransactionsByTerminal(
            @PathVariable Long terminalId) {
        log.info("GET /api/payment-terminals/{}/transactions - Fetching transactions", terminalId);
        List<TerminalTransactionResponse> responses = paymentTerminalService.getTransactionsByTerminal(terminalId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get transactions by date range
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TerminalTransactionResponse>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /api/payment-terminals/transactions - Fetching transactions between {} and {}", startDate, endDate);
        List<TerminalTransactionResponse> responses = paymentTerminalService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }
}
