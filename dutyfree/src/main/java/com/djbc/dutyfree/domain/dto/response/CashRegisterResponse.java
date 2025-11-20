package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterResponse {
    private Long id;
    private String registerNumber;
    private String name;
    private String location;

    private Boolean isOpen;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal expectedBalance;
    private BigDecimal cashInDrawer;

    private LocalDateTime openedAt;
    private String openedByUsername;   // seulement le username
    private LocalDateTime closedAt;
    private String closedByUsername;   // seulement le username

    private Boolean active;
}
