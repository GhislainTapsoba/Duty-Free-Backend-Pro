package com.djbc.dutyfree.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalTransactionResponse {

    private Long id;
    private String transactionId;

    private Long terminalId;
    private String terminalName;

    private Long paymentId;

    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String status;

    private String cardType;
    private String cardNumberMasked;
    private String cardHolderName;

    private String authorizationCode;
    private String referenceNumber;
    private String acquirerId;
    private String merchantId;

    private String terminalReceipt;
    private String customerReceipt;

    private String errorCode;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private Long responseTimeMs;

    private Boolean signatureRequired;
    private Boolean signatureVerified;
    private Boolean pinVerified;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdBy;
    private String notes;

    // Computed fields
    private Boolean successful;
    private Boolean canBeReversed;
}
