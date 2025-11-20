package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une fiche technique (recette/composition d'un produit)
 * Exemple: Fiche technique pour "Croissant" = 250g farine + 50g beurre + ...
 */
@Entity
@Table(name = "technical_sheets", indexes = {
        @Index(name = "idx_technical_sheet_code", columnList = "sheet_code"),
        @Index(name = "idx_technical_sheet_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheet extends BaseEntity {

    @Column(name = "sheet_code", nullable = false, unique = true, length = 50)
    private String sheetCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Produit fini associé à cette fiche technique
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    /**
     * Version de la fiche technique
     */
    @Column(length = 20)
    private String version = "1.0";

    /**
     * Quantité produite par cette recette
     */
    @Column(name = "output_quantity", precision = 19, scale = 3)
    private BigDecimal outputQuantity = BigDecimal.ONE;

    /**
     * Unité de sortie: PIECE, KG, L, etc.
     */
    @Column(name = "output_unit", length = 20)
    private String outputUnit = "PIECE";

    /**
     * Temps de préparation en minutes
     */
    @Column(name = "preparation_time")
    private Integer preparationTime;

    /**
     * Temps de cuisson en minutes
     */
    @Column(name = "cooking_time")
    private Integer cookingTime;

    /**
     * Niveau de difficulté: EASY, MEDIUM, HARD
     */
    @Column(length = 20)
    private String difficulty = "MEDIUM";

    /**
     * Instructions de préparation
     */
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    /**
     * Coût total des matières premières
     */
    @Column(name = "total_cost", precision = 19, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    /**
     * Coût par unité produite
     */
    @Column(name = "cost_per_unit", precision = 19, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    /**
     * Actif ou non
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Validée ou en brouillon
     */
    @Column(nullable = false)
    private Boolean validated = false;

    /**
     * Date de validation
     */
    @Column(name = "validated_at")
    private java.time.LocalDateTime validatedAt;

    /**
     * Validé par (utilisateur)
     */
    @Column(name = "validated_by", length = 50)
    private String validatedBy;

    /**
     * Liste des ingrédients/matières nécessaires
     */
    @OneToMany(mappedBy = "technicalSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TechnicalSheetItem> items = new ArrayList<>();

    /**
     * Notes additionnelles
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Ajoute un ingrédient à la fiche technique
     */
    public void addItem(TechnicalSheetItem item) {
        items.add(item);
        item.setTechnicalSheet(this);
    }

    /**
     * Retire un ingrédient de la fiche technique
     */
    public void removeItem(TechnicalSheetItem item) {
        items.remove(item);
        item.setTechnicalSheet(null);
    }

    /**
     * Calcule le coût total de la fiche technique
     */
    public void calculateTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        for (TechnicalSheetItem item : items) {
            if (item.getCost() != null) {
                total = total.add(item.getCost());
            }
        }
        this.totalCost = total;

        // Calculer le coût par unité
        if (outputQuantity != null && outputQuantity.compareTo(BigDecimal.ZERO) > 0) {
            this.costPerUnit = total.divide(outputQuantity, 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
