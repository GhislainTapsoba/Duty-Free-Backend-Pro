package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialRequest {

    @NotBlank(message = "Material code is required")
    @Size(max = 50, message = "Material code must not exceed 50 characters")
    private String materialCode;

    @NotBlank(message = "Material name is required")
    @Size(max = 200, message = "Material name must not exceed 200 characters")
    private String materialName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Material category is required")
    @Size(max = 50, message = "Material category must not exceed 50 characters")
    private String materialCategory;

    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Invalid purchase price format")
    private BigDecimal purchasePrice;

    @NotNull(message = "Quantity in stock is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity in stock must be 0 or greater")
    @Digits(integer = 16, fraction = 3, message = "Invalid quantity format")
    private BigDecimal quantityInStock;

    @DecimalMin(value = "0.0", inclusive = true, message = "Min stock level must be 0 or greater")
    @Digits(integer = 16, fraction = 3, message = "Invalid min stock level format")
    private BigDecimal minStockLevel;

    @DecimalMin(value = "0.0", inclusive = true, message = "Reorder level must be 0 or greater")
    @Digits(integer = 16, fraction = 3, message = "Invalid reorder level format")
    private BigDecimal reorderLevel;

    @DecimalMin(value = "0.0", inclusive = true, message = "Reorder quantity must be 0 or greater")
    @Digits(integer = 16, fraction = 3, message = "Invalid reorder quantity format")
    private BigDecimal reorderQuantity;

    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String supplierName;

    @Size(max = 100, message = "Supplier contact must not exceed 100 characters")
    private String supplierContact;

    private Boolean perishable = false;

    @Min(value = 0, message = "Shelf life must be 0 or greater")
    private Integer shelfLifeDays;

    @Size(max = 100, message = "Storage conditions must not exceed 100 characters")
    private String storageConditions;

    private LocalDate lastPurchaseDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Last purchase price must be 0 or greater")
    @Digits(integer = 16, fraction = 2, message = "Invalid last purchase price format")
    private BigDecimal lastPurchasePrice;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Boolean active = true;
}
