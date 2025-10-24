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
public class WastageResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private LocalDate wastageDate;
    private String reason;
    private String description;
    private String reportedBy;
    private BigDecimal valueLost;
    private Boolean approved;
    private String approvedBy;
    private LocalDate approvalDate;
}