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
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    private BigDecimal transportCost;
    private BigDecimal insuranceCost;
    private BigDecimal otherCosts;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderItemRequest {

        @NotNull(message = "Product is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(1)
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal unitPrice;

        private String notes;
    }
}