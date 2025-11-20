package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité pour tracker le nombre de passagers quotidiens
 * Permet de calculer le taux de capture (tickets / passagers)
 */
@Entity
@Table(name = "passenger_counts", indexes = {
        @Index(name = "idx_passenger_count_date", columnList = "count_date"),
        @Index(name = "idx_passenger_count_flight", columnList = "flight_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date du comptage
     */
    @Column(name = "count_date", nullable = false)
    private LocalDate countDate;

    /**
     * Nombre total de passagers
     */
    @Column(name = "total_passengers", nullable = false)
    private Integer totalPassengers = 0;

    /**
     * Nombre de passagers entrants (arrivées internationales)
     */
    @Column(name = "arriving_passengers")
    private Integer arrivingPassengers = 0;

    /**
     * Nombre de passagers sortants (départs internationaux)
     */
    @Column(name = "departing_passengers")
    private Integer departingPassengers = 0;

    /**
     * Nombre de passagers internationaux
     */
    @Column(name = "international_passengers")
    private Integer internationalPassengers = 0;

    /**
     * Numéro de vol (optionnel)
     */
    @Column(name = "flight_number", length = 20)
    private String flightNumber;

    /**
     * Compagnie aérienne (optionnel)
     */
    @Column(name = "airline", length = 100)
    private String airline;

    /**
     * Destination (optionnel)
     */
    @Column(name = "destination", length = 100)
    private String destination;

    /**
     * Type de comptage: MANUAL (saisie manuelle), AUTOMATIC (scan boarding pass), ESTIMATED (estimation)
     */
    @Column(name = "count_type", length = 20)
    private String countType = "MANUAL";

    /**
     * Notes additionnelles
     */
    @Column(length = 500)
    private String notes;

    /**
     * Créé par
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * Mis à jour par
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * Date de création
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de mise à jour
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calcule le total à partir des arrivées et départs
     */
    public void calculateTotal() {
        int arriving = arrivingPassengers != null ? arrivingPassengers : 0;
        int departing = departingPassengers != null ? departingPassengers : 0;
        this.totalPassengers = arriving + departing;
    }
}
