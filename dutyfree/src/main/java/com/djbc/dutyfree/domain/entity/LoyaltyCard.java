package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loyalty_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyCard extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(nullable = false, unique = true, length = 50)
    private String cardNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pointsBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 50)
    private String tier = "STANDARD";

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    private LocalDate lastUsedDate;

    @Column(nullable = false)
    private Integer totalPurchases = 0;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
}