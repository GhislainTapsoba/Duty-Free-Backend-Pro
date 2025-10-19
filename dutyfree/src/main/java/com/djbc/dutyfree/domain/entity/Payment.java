package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInCurrency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInXOF;

    @Column(precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(length = 100)
    private String transactionReference;

    @Column(length = 100)
    private String cardLast4Digits;

    @Column(length = 50)
    private String cardType;

    @Column(length = 100)
    private String mobileMoneyProvider;

    @Column(length = 100)
    private String mobileMoneyNumber;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Boolean verified = false;
}