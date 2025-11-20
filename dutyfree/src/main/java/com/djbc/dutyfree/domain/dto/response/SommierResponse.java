package com.djbc.dutyfree.domain.dto.response;

import com.djbc.dutyfree.domain.enums.SommierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SommierResponse {
    private Long id;
    private String sommierNumber;
    private Long purchaseOrderId;  // Juste l'ID, pas l'objet complet
    private LocalDate openingDate;
    private LocalDate closingDate;
    private BigDecimal initialValue;
    private BigDecimal currentValue;
    private BigDecimal clearedValue;
    private SommierStatus status;
    private String notes;
    private LocalDate alertDate;
    private Boolean alertSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}