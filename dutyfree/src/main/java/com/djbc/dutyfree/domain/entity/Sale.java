package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_date", columnList = "saleDate"),
        @Index(name = "idx_sale_number", columnList = "saleNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String saleNumber;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status = SaleStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL)
    private Receipt receipt;

    // Boarding pass information
    @Column(length = 100)
    private String passengerName;

    @Column(length = 50)
    private String flightNumber;

    @Column(length = 100)
    private String airline;

    @Column(length = 100)
    private String destination;
}