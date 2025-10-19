package com.djbc.dutyfree.domain.dto.request;

import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    private String transactionReference;
    private String cardLast4Digits;
    private String cardType;
    private String mobileMoneyProvider;
    private String mobileMoneyNumber;
    private String notes;
}