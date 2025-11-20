package com.djbc.dutyfree.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPriceResponse {

    private Long id;
    private String name;
    private String description;
    private Long productId;
    private String productCode;
    private String productName;
    private String priceType;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime timeFrom;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime timeUntil;

    private String daysOfWeek;
    private Integer priority;
    private String periodType;
    private Boolean active;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean currentlyValid;
    private BigDecimal effectivePrice; // Calculated price based on product's base price
}
