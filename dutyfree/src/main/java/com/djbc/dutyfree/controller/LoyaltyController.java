package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.LoyaltyCard;
import com.djbc.dutyfree.service.LoyaltyService;
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
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Loyalty", description = "Loyalty card management APIs")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Create loyalty card", description = "Create a loyalty card for a customer")
    public ResponseEntity<ApiResponse<LoyaltyCard>> createLoyaltyCard(@PathVariable Long customerId) {
        LoyaltyCard card = loyaltyService.createLoyaltyCard(customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loyalty card created successfully", card));
    }

    @PostMapping("/{cardNumber}/points/add")
    @Operation(summary = "Add points", description = "Add loyalty points based on purchase amount")
    public ResponseEntity<ApiResponse<LoyaltyCard>> addPoints(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal purchaseAmount) {
        LoyaltyCard card = loyaltyService.addPoints(cardNumber, purchaseAmount);
        return ResponseEntity.ok(ApiResponse.success("Points added successfully", card));
    }

    @PostMapping("/{cardNumber}/points/redeem")
    @Operation(summary = "Redeem points", description = "Redeem loyalty points")
    public ResponseEntity<ApiResponse<LoyaltyCard>> redeemPoints(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal points) {
        LoyaltyCard card = loyaltyService.redeemPoints(cardNumber, points);
        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully", card));
    }

    @PostMapping("/{cardNumber}/wallet/add")
    @Operation(summary = "Add to wallet", description = "Add amount to loyalty card wallet")
    public ResponseEntity<ApiResponse<LoyaltyCard>> addToWallet(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal amount) {
        LoyaltyCard card = loyaltyService.addToWallet(cardNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Amount added to wallet successfully", card));
    }

    @PostMapping("/{cardNumber}/wallet/deduct")
    @Operation(summary = "Deduct from wallet", description = "Deduct amount from loyalty card wallet")
    public ResponseEntity<ApiResponse<LoyaltyCard>> deductFromWallet(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal amount) {
        LoyaltyCard card = loyaltyService.deductFromWallet(cardNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Amount deducted from wallet successfully", card));
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get card by number", description = "Get loyalty card details by card number")
    public ResponseEntity<ApiResponse<LoyaltyCard>> getCardByNumber(@PathVariable String cardNumber) {
        LoyaltyCard card = loyaltyService.getCardByNumber(cardNumber);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get card by customer", description = "Get loyalty card by customer ID")
    public ResponseEntity<ApiResponse<LoyaltyCard>> getCardByCustomer(@PathVariable Long customerId) {
        LoyaltyCard card = loyaltyService.getCardByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Get expiring cards", description = "Get loyalty cards expiring soon")
    public ResponseEntity<ApiResponse<List<LoyaltyCard>>> getExpiringCards(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<LoyaltyCard> cards = loyaltyService.getExpiringCards(daysAhead);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{cardNumber}/renew")
    @Operation(summary = "Renew card", description = "Renew a loyalty card")
    public ResponseEntity<ApiResponse<LoyaltyCard>> renewCard(@PathVariable String cardNumber) {
        LoyaltyCard card = loyaltyService.renewCard(cardNumber);
        return ResponseEntity.ok(ApiResponse.success("Card renewed successfully", card));
    }

    @PostMapping("/{cardNumber}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Deactivate card", description = "Deactivate a loyalty card")
    public ResponseEntity<ApiResponse<Void>> deactivateCard(@PathVariable String cardNumber) {
        loyaltyService.deactivateCard(cardNumber);
        return ResponseEntity.ok(ApiResponse.success("Card deactivated successfully", null));
    }
}