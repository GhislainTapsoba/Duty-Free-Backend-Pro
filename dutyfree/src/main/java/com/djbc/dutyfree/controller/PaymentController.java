package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.PaymentRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.Payment;
import com.djbc.dutyfree.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sale/{saleId}")
    @Operation(summary = "Process payment", description = "Process payment for a sale")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @PathVariable Long saleId,
            @Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(saleId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", payment));
    }

    @GetMapping("/sale/{saleId}")
    @Operation(summary = "Get payments by sale", description = "Get all payments for a sale")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsBySale(@PathVariable Long saleId) {
        List<Payment> payments = paymentService.getPaymentsBySale(saleId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/sale/{saleId}/total")
    @Operation(summary = "Get total paid amount", description = "Get total amount paid for a sale")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaidAmount(@PathVariable Long saleId) {
        BigDecimal total = paymentService.getTotalPaidAmount(saleId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @PostMapping("/{paymentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Verify payment", description = "Verify a payment transaction")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.verifyPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Return all payments")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}