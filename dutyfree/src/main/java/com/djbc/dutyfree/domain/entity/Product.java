package com.djbc.dutyfree.domain.entity;

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

    @Column(name = "name_fr", nullable = false, length = 200)
    private String nameFr;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "description_fr", length = 1000)
    private String descriptionFr;

    @Column(name = "description_en", length = 1000)
    private String descriptionEn;

    @Column(unique = true, length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "purchase_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "selling_price_xof", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellingPriceXOF;

    @Column(name = "selling_price_eur", precision = 19, scale = 2)
    private BigDecimal sellingPriceEUR;

    @Column(name = "selling_price_usd", precision = 19, scale = 2)
    private BigDecimal sellingPriceUSD;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "track_stock", nullable = false)
    private Boolean trackStock = true;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 0;

    @Column(length = 50)
    private String unit = "PIECE";

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Stock> stocks = new ArrayList<>();
}