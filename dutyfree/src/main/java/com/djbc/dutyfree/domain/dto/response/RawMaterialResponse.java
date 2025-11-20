package com.djbc.dutyfree.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialResponse {

    private Long id;
    private String materialCode;
    private String materialName;
    private String description;
    private String materialCategory;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal quantityInStock;
    private BigDecimal minStockLevel;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
    private String supplierName;
    private String supplierContact;
    private Boolean perishable;
    private Integer shelfLifeDays;
    private String storageConditions;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastPurchaseDate;

    private BigDecimal lastPurchasePrice;
    private String notes;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean isLowStock;
    private Boolean needsReorder;
    private BigDecimal stockValue; // quantityInStock * purchasePrice
}
