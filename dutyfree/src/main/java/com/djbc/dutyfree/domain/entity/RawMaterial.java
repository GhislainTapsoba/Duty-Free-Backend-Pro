package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entité représentant une matière première (ingrédient)
 * Exemple: Farine, Sucre, Lait, etc.
 */
@Entity
@Table(name = "raw_materials", indexes = {
        @Index(name = "idx_raw_material_code", columnList = "material_code"),
        @Index(name = "idx_raw_material_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterial extends BaseEntity {

    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 200)
    private String materialName;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "supplier_contact", length = 100)
    private String supplierContact;

    /**
     * Unité de mesure: KG, L, UNIT, G, ML, etc.
     */
    @Column(nullable = false, length = 20)
    private String unit = "KG";

    /**
     * Prix d'achat unitaire
     */
    @Column(name = "purchase_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    /**
     * Quantité en stock
     */
    @Column(name = "quantity_in_stock", precision = 19, scale = 3)
    private BigDecimal quantityInStock = BigDecimal.ZERO;

    /**
     * Niveau minimum de stock (alerte)
     */
    @Column(name = "min_stock_level", precision = 19, scale = 3)
    private BigDecimal minStockLevel = BigDecimal.ZERO;

    /**
     * Niveau de réapprovisionnement
     */
    @Column(name = "reorder_level", precision = 19, scale = 3)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    /**
     * Quantité de réapprovisionnement recommandée
     */
    @Column(name = "reorder_quantity", precision = 19, scale = 3)
    private BigDecimal reorderQuantity = BigDecimal.ZERO;

    /**
     * Catégorie de matière: FOOD, BEVERAGE, PACKAGING, CLEANING, OTHER
     */
    @Column(name = "material_category", length = 50)
    private String materialCategory = "FOOD";

    /**
     * Actif ou non
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Périssable ou non
     */
    @Column(nullable = false)
    private Boolean perishable = false;

    /**
     * Durée de conservation en jours (si périssable)
     */
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    /**
     * Conditions de stockage
     */
    @Column(name = "storage_conditions", length = 500)
    private String storageConditions;

    /**
     * Date du dernier achat
     */
    @Column(name = "last_purchase_date")
    private java.time.LocalDate lastPurchaseDate;

    /**
     * Prix du dernier achat
     */
    @Column(name = "last_purchase_price", precision = 19, scale = 2)
    private BigDecimal lastPurchasePrice;

    /**
     * Notes additionnelles
     */
    @Column(length = 1000)
    private String notes;
}
