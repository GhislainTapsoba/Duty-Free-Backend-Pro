package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "French name is required")
    private String nameFr;

    @NotBlank(message = "English name is required")
    private String nameEn;

    private String descriptionFr;
    private String descriptionEn;

    private String barcode;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long supplierId;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price in XOF is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal sellingPriceXOF;

    private BigDecimal sellingPriceEUR;
    private BigDecimal sellingPriceUSD;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal taxRate;

    private String imageUrl;
    private Boolean active;
    private Boolean trackStock;

    @Min(0)
    private Integer minStockLevel;

    @Min(0)
    private Integer reorderLevel;

    private String unit;
}