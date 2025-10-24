package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyCardResponse {
    private Long id;
    private String cardNumber;
    private Long customerId;
    private String customerName;
    private Integer points;
    private BigDecimal walletBalance;
    private String tierLevel;
    private LocalDate expiryDate;
    private Boolean active;
    private LocalDate lastUsedDate;
}