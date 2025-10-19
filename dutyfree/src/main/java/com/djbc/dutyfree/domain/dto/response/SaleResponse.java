package com.djbc.dutyfree.domain.dto.response;

import com.djbc.dutyfree.domain.enums.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private LocalDateTime saleDate;
    private String cashierName;
    private String customerName;
    private String cashRegisterNumber;
    private SaleStatus status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private List<SaleItemResponse> items;
    private List<PaymentResponse> payments;
    private String receiptNumber;
    private String passengerName;
    private String flightNumber;
    private String airline;
    private String destination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal totalPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private String paymentMethod;
        private String currency;
        private BigDecimal amount;
        private BigDecimal amountInXOF;
        private LocalDateTime paymentDate;
        private String transactionReference;
    }
}