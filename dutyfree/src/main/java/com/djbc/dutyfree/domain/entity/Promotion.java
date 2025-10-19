package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(length = 20)
    private String discountType = "PERCENTAGE"; // PERCENTAGE, FIXED_AMOUNT

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal minimumPurchaseAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean stackable = false;

    private Integer usageLimit;

    @Column(nullable = false)
    private Integer usageCount = 0;

    @ManyToMany
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> applicableProducts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "promotion_categories",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> applicableCategories = new ArrayList<>();

    @Column(nullable = false)
    private Boolean applyToAllProducts = false;

    @Column(length = 1000)
    private String terms;
}