package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un menu/formule composé de plusieurs produits
 * Exemple: "Petit déjeuner" = café + croissant + jus d'orange
 */
@Entity
@Table(name = "product_bundles", indexes = {
        @Index(name = "idx_bundle_code", columnList = "bundle_code"),
        @Index(name = "idx_bundle_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBundle extends BaseEntity {

    @Column(name = "bundle_code", nullable = false, unique = true, length = 50)
    private String bundleCode;

    @Column(name = "name_fr", nullable = false, length = 200)
    private String nameFr;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "description_fr", length = 1000)
    private String descriptionFr;

    @Column(name = "description_en", length = 1000)
    private String descriptionEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Prix du bundle en XOF
     * Si null, le prix est calculé automatiquement à partir des items
     */
    @Column(name = "bundle_price_xof", precision = 19, scale = 2)
    private BigDecimal bundlePriceXOF;

    /**
     * Prix du bundle en EUR
     */
    @Column(name = "bundle_price_eur", precision = 19, scale = 2)
    private BigDecimal bundlePriceEUR;

    /**
     * Prix du bundle en USD
     */
    @Column(name = "bundle_price_usd", precision = 19, scale = 2)
    private BigDecimal bundlePriceUSD;

    /**
     * Remise en pourcentage par rapport à l'achat séparé des items
     */
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    /**
     * Type de bundle: MENU (repas complet), COMBO (produits groupés), FORMULA (formule spéciale)
     */
    @Column(name = "bundle_type", length = 50)
    private String bundleType = "MENU"; // MENU, COMBO, FORMULA

    /**
     * Image du bundle
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Date de début de validité de la formule
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * Date de fin de validité de la formule
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * Actif ou non
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Disponible uniquement à certaines heures (ex: petit déjeuner 6h-11h)
     */
    @Column(name = "time_restricted")
    private Boolean timeRestricted = false;

    @Column(name = "start_time")
    private String startTime; // Format: "06:00"

    @Column(name = "end_time")
    private String endTime; // Format: "11:00"

    /**
     * Nombre maximum de ventes par jour (pour promotions limitées)
     */
    @Column(name = "daily_limit")
    private Integer dailyLimit;

    /**
     * Compteur des ventes aujourd'hui
     */
    @Column(name = "today_sold_count")
    private Integer todaySoldCount = 0;

    /**
     * Liste des produits composant le bundle
     */
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BundleItem> items = new ArrayList<>();

    /**
     * Ajoute un produit au bundle
     */
    public void addItem(BundleItem item) {
        items.add(item);
        item.setBundle(this);
    }

    /**
     * Retire un produit du bundle
     */
    public void removeItem(BundleItem item) {
        items.remove(item);
        item.setBundle(null);
    }

    /**
     * Vérifie si le bundle est valide à la date/heure actuelle
     */
    public boolean isValidNow() {
        LocalDateTime now = LocalDateTime.now();

        // Vérifier si actif
        if (!active) {
            return false;
        }

        // Vérifier les dates de validité
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        // Vérifier les restrictions horaires
        if (timeRestricted && startTime != null && endTime != null) {
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());
            if (currentTime.compareTo(startTime) < 0 || currentTime.compareTo(endTime) > 0) {
                return false;
            }
        }

        // Vérifier la limite journalière
        if (dailyLimit != null && todaySoldCount != null && todaySoldCount >= dailyLimit) {
            return false;
        }

        return true;
    }

    /**
     * Calcule le prix total si acheté séparément
     */
    public BigDecimal calculateSeparatePrice(String currency) {
        BigDecimal total = BigDecimal.ZERO;
        for (BundleItem item : items) {
            BigDecimal itemPrice = switch (currency.toUpperCase()) {
                case "EUR" -> item.getProduct().getSellingPriceEUR();
                case "USD" -> item.getProduct().getSellingPriceUSD();
                default -> item.getProduct().getSellingPriceXOF();
            };
            if (itemPrice != null) {
                total = total.add(itemPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    /**
     * Calcule le prix du bundle dans la devise donnée
     */
    public BigDecimal getBundlePrice(String currency) {
        BigDecimal bundlePrice = switch (currency.toUpperCase()) {
            case "EUR" -> bundlePriceEUR;
            case "USD" -> bundlePriceUSD;
            default -> bundlePriceXOF;
        };

        // Si le prix du bundle n'est pas défini, calculer avec la remise
        if (bundlePrice == null) {
            BigDecimal separatePrice = calculateSeparatePrice(currency);
            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = separatePrice.multiply(discountPercentage).divide(new BigDecimal(100));
                return separatePrice.subtract(discount);
            }
            return separatePrice;
        }

        return bundlePrice;
    }
}
