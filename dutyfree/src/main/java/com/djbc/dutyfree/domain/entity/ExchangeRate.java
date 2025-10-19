package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"currency", "effectiveDate"}),
        indexes = @Index(name = "idx_currency_date", columnList = "currency,effectiveDate")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal rateToXOF;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 500)
    private String source;

    @Column(length = 1000)
    private String notes;
}