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

    @Column(name = "card_number", nullable = false, unique = true, length = 50)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "wallet_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(name = "tier_level", length = 20)
    private String tierLevel = "BRONZE"; // BRONZE, SILVER, GOLD, PLATINUM

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;
}