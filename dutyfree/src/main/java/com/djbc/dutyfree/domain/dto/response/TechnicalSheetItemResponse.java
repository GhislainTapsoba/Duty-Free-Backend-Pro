package com.djbc.dutyfree.domain.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheetItemResponse {

    private Long id;
    private Long rawMaterialId;
    private String rawMaterialCode;
    private String rawMaterialName;
    private String rawMaterialUnit;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal cost;
    private Integer displayOrder;
    private String notes;
    private Boolean optional;
    private BigDecimal conversionFactor;

    // Computed fields
    private BigDecimal rawMaterialPurchasePrice;
    private String rawMaterialCategory;
}
