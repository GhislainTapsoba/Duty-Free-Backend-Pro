package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un produit dans un bundle/menu
 * Exemple: dans "Petit déjeuner", un BundleItem serait "1x Café"
 */
@Entity
@Table(name = "bundle_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private ProductBundle bundle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Quantité du produit dans le bundle
     */
    @Column(nullable = false)
    private Integer quantity = 1;

    /**
     * Le produit est-il optionnel dans le bundle?
     * Exemple: "Sucre" dans un café peut être optionnel
     */
    @Column(nullable = false)
    private Boolean optional = false;

    /**
     * Ordre d'affichage dans le bundle
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Notes sur l'item (ex: "Chaud", "Sans sucre", etc.)
     */
    @Column(length = 500)
    private String notes;

    /**
     * Est-ce un produit substituable?
     * Exemple: café peut être remplacé par thé
     */
    @Column(name = "substitutable")
    private Boolean substitutable = false;

    /**
     * Groupe de substitution (pour produits interchangeables)
     * Exemple: tous les produits avec substitutionGroup="BEVERAGE_HOT" peuvent se remplacer
     */
    @Column(name = "substitution_group", length = 50)
    private String substitutionGroup;
}
