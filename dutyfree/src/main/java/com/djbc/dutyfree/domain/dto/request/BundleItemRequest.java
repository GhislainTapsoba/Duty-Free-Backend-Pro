package com.djbc.dutyfree.domain.dto.request;

import lombok.Data;

@Data
public class BundleItemRequest {
    private Long productId;
    private Integer quantity = 1;
    private Boolean optional = false;
    private Integer displayOrder = 0;
    private String notes;
    private Boolean substitutable = false;
    private String substitutionGroup;
}
