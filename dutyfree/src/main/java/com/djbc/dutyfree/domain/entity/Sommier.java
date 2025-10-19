package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.SommierStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sommiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sommier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String sommierNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private LocalDate openingDate;

    private LocalDate closingDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal initialValue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal clearedValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SommierStatus status = SommierStatus.ACTIVE;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "sommier", cascade = CascadeType.ALL)
    private List<Stock> stocks = new ArrayList<>();

    private LocalDate alertDate;

    private Boolean alertSent = false;
}