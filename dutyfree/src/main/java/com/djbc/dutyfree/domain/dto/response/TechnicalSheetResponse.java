package com.djbc.dutyfree.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheetResponse {

    private Long id;
    private String sheetCode;
    private String name;
    private String description;
    private Long productId;
    private String productCode;
    private String productName;
    private String version;
    private BigDecimal outputQuantity;
    private String outputUnit;
    private Integer preparationTime;
    private Integer cookingTime;
    private String difficulty;
    private String instructions;
    private BigDecimal totalCost;
    private BigDecimal costPerUnit;
    private Boolean active;
    private Boolean validated;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validatedAt;

    private String validatedBy;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    @Builder.Default
    private List<TechnicalSheetItemResponse> items = new ArrayList<>();

    // Computed fields
    private Integer totalItems;
    private Integer totalTime; // preparationTime + cookingTime
}
