package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleItemResponse {
    private Long id;
    private Long productId;
    private String productNameFr;
    private String productNameEn;
    private String productSku;
    private String productBarcode;
    private Integer quantity;
    private Boolean optional;
    private Integer displayOrder;
    private String notes;
    private Boolean substitutable;
    private String substitutionGroup;
}
