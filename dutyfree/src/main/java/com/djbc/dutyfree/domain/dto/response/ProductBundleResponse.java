package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundleResponse {
    private Long id;
    private String bundleCode;
    private String nameFr;
    private String nameEn;
    private String descriptionFr;
    private String descriptionEn;
    private Long categoryId;
    private String categoryName;
    private BigDecimal bundlePriceXOF;
    private BigDecimal bundlePriceEUR;
    private BigDecimal bundlePriceUSD;
    private BigDecimal discountPercentage;
    private String bundleType;
    private String imageUrl;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active;
    private Boolean timeRestricted;
    private String startTime;
    private String endTime;
    private Integer dailyLimit;
    private Integer todaySoldCount;
    private Boolean isValidNow;
    private BigDecimal separatePriceXOF;
    private BigDecimal savingsXOF;
    @Builder.Default
    private List<BundleItemResponse> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
