package com.djbc.dutyfree.domain.entity;

import com.djbc.dutyfree.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_barcode", columnList = "barcode"),
        @Index(name = "idx_sku", columnList = "sku")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nameFr;

    @Column(nullable = false, length = 200)
    private String nameEn;

    @Column(length = 1000)
    private String descriptionFr;

    @Column(length = 1000)
    private String descriptionEn;

    @Column(unique = true, length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal sellingPriceXOF;

    @Column(precision = 19, scale = 2)
    private BigDecimal sellingPriceEUR;

    @Column(precision = 19, scale = 2)
    private BigDecimal sellingPriceUSD;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean trackStock = true;

    private Integer minStockLevel = 0;

    private Integer reorderLevel = 0;

    @Column(length = 50)
    private String unit = "PIECE";

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Stock> stocks = new ArrayList<>();
}