package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.CreateLoyaltyCardRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.LoyaltyCardResponse;
import com.djbc.dutyfree.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Loyalty", description = "Loyalty card management APIs")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @PostMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Create loyalty card")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> createCard(
            @PathVariable Long customerId,
            @RequestBody(required = false) CreateLoyaltyCardRequest request) {
        
        if (request == null) {
            request = new CreateLoyaltyCardRequest();
        }
        request.setCustomerId(customerId);
        
        LoyaltyCardResponse card = loyaltyService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loyalty card created successfully", card));
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get card by number")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> getByCardNumber(@PathVariable String cardNumber) {
        LoyaltyCardResponse card = loyaltyService.getByCardNumber(cardNumber);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get card by customer")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> getByCustomer(@PathVariable Long customerId) {
        LoyaltyCardResponse card = loyaltyService.getByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get expiring cards")
    public ResponseEntity<ApiResponse<List<LoyaltyCardResponse>>> getExpiringCards(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<LoyaltyCardResponse> cards = loyaltyService.getExpiringCards(daysAhead);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{cardNumber}/points/add")
    @Operation(summary = "Add points")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> addPoints(
            @PathVariable String cardNumber,
            @RequestParam Integer points) {
        LoyaltyCardResponse card = loyaltyService.addPoints(cardNumber, points);
        return ResponseEntity.ok(ApiResponse.success("Points added successfully", card));
    }

    @PostMapping("/{cardNumber}/points/redeem")
    @Operation(summary = "Redeem points")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> redeemPoints(
            @PathVariable String cardNumber,
            @RequestParam Integer points) {
        LoyaltyCardResponse card = loyaltyService.redeemPoints(cardNumber, points);
        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully", card));
    }

    @PostMapping("/{cardNumber}/wallet/add")
    @Operation(summary = "Add to wallet")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> addToWallet(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal amount) {
        LoyaltyCardResponse card = loyaltyService.addToWallet(cardNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Amount added to wallet", card));
    }

    @PostMapping("/{cardNumber}/wallet/deduct")
    @Operation(summary = "Deduct from wallet")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> deductFromWallet(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal amount) {
        LoyaltyCardResponse card = loyaltyService.deductFromWallet(cardNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Amount deducted from wallet", card));
    }

    @PostMapping("/{cardNumber}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Renew card")
    public ResponseEntity<ApiResponse<LoyaltyCardResponse>> renewCard(@PathVariable String cardNumber) {
        LoyaltyCardResponse card = loyaltyService.renewCard(cardNumber);
        return ResponseEntity.ok(ApiResponse.success("Card renewed successfully", card));
    }

    @PostMapping("/{cardNumber}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Deactivate card")
    public ResponseEntity<ApiResponse<Void>> deactivateCard(@PathVariable String cardNumber) {
        loyaltyService.deactivateCard(cardNumber);
        return ResponseEntity.ok(ApiResponse.success("Card deactivated successfully", null));
    }
}