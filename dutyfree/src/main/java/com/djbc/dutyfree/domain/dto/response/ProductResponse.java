package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String nameFr;
    private String nameEn;
    private String descriptionFr;
    private String descriptionEn;
    private String barcode;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal purchasePrice;
    private BigDecimal priceXOF; // <-- au lieu de sellingPriceXOF
    private BigDecimal priceEUR;
    private BigDecimal priceUSD;
    private BigDecimal taxRate;
    private String imageUrl;
    private Boolean active;
    private Boolean trackStock;
    private Integer currentStock;
    private Integer minStockLevel;
    private Integer reorderLevel;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
