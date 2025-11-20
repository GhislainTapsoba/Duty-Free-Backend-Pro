package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entité pour la gestion des prix programmés et dynamiques
 * Permet de définir des prix différents selon l'heure, le jour, la saison
 */
@Entity
@Table(name = "scheduled_prices", indexes = {
        @Index(name = "idx_scheduled_price_product", columnList = "product_id"),
        @Index(name = "idx_scheduled_price_dates", columnList = "valid_from, valid_until"),
        @Index(name = "idx_scheduled_price_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPrice extends BaseEntity {

    /**
     * Nom de la règle de prix
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description
     */
    @Column(length = 500)
    private String description;

    /**
     * Produit concerné
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Type de prix: FIXED (prix fixe), DISCOUNT (réduction), MARKUP (majoration)
     */
    @Column(name = "price_type", nullable = false, length = 20)
    private String priceType = "FIXED";

    /**
     * Nouveau prix (si FIXED) ou montant de réduction/majoration
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Pourcentage de réduction/majoration (alternatif à amount)
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    /**
     * Devise
     */
    @Column(length = 3)
    private String currency = "XOF";

    /**
     * Date de début de validité
     */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /**
     * Date de fin de validité
     */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    /**
     * Heure de début (pour prix horaires)
     */
    @Column(name = "time_from")
    private LocalTime timeFrom;

    /**
     * Heure de fin (pour prix horaires)
     */
    @Column(name = "time_until")
    private LocalTime timeUntil;

    /**
     * Jours de la semaine applicables (séparés par virgule: MONDAY,TUESDAY,...)
     */
    @Column(name = "days_of_week", length = 100)
    private String daysOfWeek;

    /**
     * Priorité (si plusieurs règles s'appliquent, la plus haute priorité gagne)
     */
    @Column(nullable = false)
    private Integer priority = 0;

    /**
     * Type de période: DAILY, WEEKLY, SEASONAL, PROMOTIONAL, SPECIAL_EVENT
     */
    @Column(name = "period_type", length = 20)
    private String periodType = "PROMOTIONAL";

    /**
     * Est actif
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Notes
     */
    @Column(length = 500)
    private String notes;

    /**
     * Check if this price rule is currently valid
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        if (!active) return false;

        // Check date validity
        if (validFrom != null && today.isBefore(validFrom)) return false;
        if (validUntil != null && today.isAfter(validUntil)) return false;

        // Check time validity
        if (timeFrom != null && timeUntil != null) {
            if (currentTime.isBefore(timeFrom) || currentTime.isAfter(timeUntil)) {
                return false;
            }
        }

        // Check day of week
        if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
            DayOfWeek currentDay = today.getDayOfWeek();
            if (!daysOfWeek.contains(currentDay.name())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculate the effective price based on this rule
     */
    public BigDecimal calculatePrice(BigDecimal basePrice) {
        if (basePrice == null) return BigDecimal.ZERO;

        switch (priceType) {
            case "FIXED":
                return amount;

            case "DISCOUNT":
                if (percentage != null) {
                    BigDecimal discount = basePrice.multiply(percentage).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    return basePrice.subtract(discount);
                } else if (amount != null) {
                    return basePrice.subtract(amount).max(BigDecimal.ZERO);
                }
                return basePrice;

            case "MARKUP":
                if (percentage != null) {
                    BigDecimal markup = basePrice.multiply(percentage).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    return basePrice.add(markup);
                } else if (amount != null) {
                    return basePrice.add(amount);
                }
                return basePrice;

            default:
                return basePrice;
        }
    }
}
