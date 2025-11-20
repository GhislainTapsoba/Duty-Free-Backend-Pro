package com.djbc.dutyfree.domain.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductBundleRequest {
    private String bundleCode;
    private String nameFr;
    private String nameEn;
    private String descriptionFr;
    private String descriptionEn;
    private Long categoryId;
    private BigDecimal bundlePriceXOF;
    private BigDecimal bundlePriceEUR;
    private BigDecimal bundlePriceUSD;
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    private String bundleType = "MENU"; // MENU, COMBO, FORMULA
    private String imageUrl;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active = true;
    private Boolean timeRestricted = false;
    private String startTime;
    private String endTime;
    private Integer dailyLimit;
    private List<BundleItemRequest> items = new ArrayList<>();
}
