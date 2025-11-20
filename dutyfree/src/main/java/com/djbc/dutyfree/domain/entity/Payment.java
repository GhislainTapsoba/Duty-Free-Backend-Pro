package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;  // ← AJOUTEZ CETTE LIGNE
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @JsonIgnore  // ← AJOUTEZ CETTE LIGNE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "amount_in_currency", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInCurrency;

    @Column(name = "amount_in_xof", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInXOF;

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "card_last4_digits", length = 100)
    private String cardLast4Digits;

    @Column(name = "card_type", length = 50)
    private String cardType;

    @Column(name = "mobile_money_provider", length = 100)
    private String mobileMoneyProvider;

    @Column(name = "mobile_money_number", length = 100)
    private String mobileMoneyNumber;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Boolean verified = false;
    
    // ← SI BESOIN, ajoutez un champ saleId pour le frontend
    @Transient
    public Long getSaleId() {
        return sale != null ? sale.getId() : null;
    }
}