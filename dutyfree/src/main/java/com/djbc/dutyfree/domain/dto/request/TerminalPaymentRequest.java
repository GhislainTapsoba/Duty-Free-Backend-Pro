package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalPaymentRequest {

    @NotNull(message = "Terminal ID is required")
    private Long terminalId;

    private Long paymentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "XOF";

    @Pattern(regexp = "SALE|REFUND|CANCELLATION|PREAUTH|COMPLETION|VOID|REVERSAL",
             message = "Transaction type must be SALE, REFUND, CANCELLATION, PREAUTH, COMPLETION, VOID, or REVERSAL")
    private String transactionType = "SALE";

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Optional fields for specific transaction types
    private String originalTransactionId; // For refunds/reversals
    private Boolean requireSignature = false;
}
