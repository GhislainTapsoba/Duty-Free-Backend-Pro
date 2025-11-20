package com.djbc.dutyfree.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterRequest {
    private String registerNumber;
    private String name;
    private String location;

    private Boolean isOpen;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal expectedBalance;
    private BigDecimal cashInDrawer;

    private Long openedById; // id de l'utilisateur qui ouvre
    private Long closedById; // id de l'utilisateur qui ferme

    private Boolean active;
}
