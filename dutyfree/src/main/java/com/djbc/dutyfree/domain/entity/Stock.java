package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stocks", indexes = {
        @Index(name = "idx_product_location", columnList = "product_id,location")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sommier_id")
    private Sommier sommier;

    @Column(name = "quantity", nullable = false)  // ← EXPLICITE
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)  // ← EXPLICITE
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)  // ← EXPLICITE
    private Integer availableQuantity = 0;

    @Column(length = 100)
    private String location;

    @Column(length = 50)
    private String lotNumber;

    private LocalDate expiryDate;

    private LocalDate receivedDate;
}