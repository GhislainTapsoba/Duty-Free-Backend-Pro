package com.djbc.dutyfree.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ← AJOUTER
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem extends BaseEntity {

    @JsonIgnore  // ← AJOUTER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @JsonIgnore  // ← AJOUTER (si Product a aussi des relations)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantityOrdered;

    @Column(nullable = false)
    private Integer quantityReceived = 0;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String notes;

    @JsonIgnore  // ← AJOUTER
    @ManyToOne
    @JoinColumn(name = "sommier_id")
    private Sommier sommier;
}