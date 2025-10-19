package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cash_registers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashRegister extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String registerNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean isOpen = false;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opened_by_user_id")
    private User openedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_user_id")
    private User closedBy;

    @Column(precision = 19, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal expectedBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal cashInDrawer = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cashRegister", cascade = CascadeType.ALL)
    private List<Sale> sales = new ArrayList<>();

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 100)
    private String terminalId;
}