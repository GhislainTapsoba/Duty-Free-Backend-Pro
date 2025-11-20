package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entité représentant un ingrédient/matière première dans une fiche technique
 * Exemple: "250g de farine" dans la recette du croissant
 */
@Entity
@Table(name = "technical_sheet_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheetItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_sheet_id", nullable = false)
    private TechnicalSheet technicalSheet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    /**
     * Quantité nécessaire
     */
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    /**
     * Unité de mesure: KG, L, G, ML, PIECE, etc.
     * (peut être différente de l'unité de base de la matière première)
     */
    @Column(nullable = false, length = 20)
    private String unit;

    /**
     * Coût de cet ingrédient pour cette recette
     * (calculé automatiquement : quantité × prix unitaire)
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal cost;

    /**
     * Ordre d'affichage / d'utilisation dans la recette
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Notes sur cet ingrédient (ex: "À température ambiante", "Préchauffer", etc.)
     */
    @Column(length = 500)
    private String notes;

    /**
     * Est-ce un ingrédient optionnel?
     */
    @Column(nullable = false)
    private Boolean optional = false;

    /**
     * Facteur de conversion si l'unité est différente de l'unité de base
     * Exemple: si matière en KG mais recette en G, facteur = 1000
     */
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    /**
     * Calcule le coût de cet item
     */
    public void calculateCost() {
        if (rawMaterial != null && quantity != null && rawMaterial.getPurchasePrice() != null) {
            // Convertir la quantité dans l'unité de base si nécessaire
            BigDecimal baseQuantity = quantity.multiply(conversionFactor);
            this.cost = baseQuantity.multiply(rawMaterial.getPurchasePrice());
        }
    }
}
