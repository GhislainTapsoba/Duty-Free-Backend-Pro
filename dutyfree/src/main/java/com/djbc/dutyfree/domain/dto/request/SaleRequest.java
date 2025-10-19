package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import javax.validation.constraints.Min;
import javax.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull(message = "Cash register is required")
    private Long cashRegisterId;

    private Long customerId;

    @NotEmpty(message = "Sale items cannot be empty")
    @Valid
    private List<SaleItemRequest> items;

    @Valid
    private List<PaymentRequest> payments;

    private BigDecimal discount;
    private String notes;

    // Boarding pass info
    private String passengerName;
    private String flightNumber;
    private String airline;
    private String destination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemRequest {

        @NotNull(message = "Product is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(1)
        private Integer quantity;

        private BigDecimal discount;
        private Long promotionId;
    }
}