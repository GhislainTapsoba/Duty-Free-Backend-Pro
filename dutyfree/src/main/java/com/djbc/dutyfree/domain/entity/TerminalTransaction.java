package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Terminal Transaction
 * Tracks all interactions with payment terminals
 */
@Entity
@Table(name = "terminal_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private PaymentTerminal terminal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "card_type", length = 20)
    private String cardType; // VISA, MASTERCARD, AMEX, etc.

    @Column(name = "card_number_masked", length = 20)
    private String cardNumberMasked; // e.g., "****1234"

    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    @Column(name = "authorization_code", length = 50)
    private String authorizationCode;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "acquirer_id", length = 50)
    private String acquirerId;

    @Column(name = "merchant_id", length = 50)
    private String merchantId;

    @Column(name = "terminal_receipt", columnDefinition = "TEXT")
    private String terminalReceipt;

    @Column(name = "customer_receipt", columnDefinition = "TEXT")
    private String customerReceipt;

    @Column(name = "error_code", length = 20)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "signature_required")
    private Boolean signatureRequired = false;

    @Column(name = "signature_verified")
    private Boolean signatureVerified;

    @Column(name = "pin_verified")
    private Boolean pinVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "notes", length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    public enum TransactionType {
        SALE,           // Vente
        REFUND,         // Remboursement
        CANCELLATION,   // Annulation
        PREAUTH,        // Pré-autorisation
        COMPLETION,     // Complétion de pré-auth
        VOID,           // Annulation du jour
        REVERSAL        // Contre-passation
    }

    public enum TransactionStatus {
        PENDING,        // En attente
        PROCESSING,     // En traitement
        APPROVED,       // Approuvée
        DECLINED,       // Refusée
        TIMEOUT,        // Timeout
        ERROR,          // Erreur
        CANCELLED,      // Annulée
        REVERSED        // Contre-passée
    }

    /**
     * Mark transaction as completed
     */
    public void complete(TransactionStatus finalStatus) {
        this.status = finalStatus;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.responseTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Check if transaction was successful
     */
    public boolean isSuccessful() {
        return status == TransactionStatus.APPROVED;
    }

    /**
     * Check if transaction can be reversed
     */
    public boolean canBeReversed() {
        return status == TransactionStatus.APPROVED &&
                (transactionType == TransactionType.SALE || transactionType == TransactionType.PREAUTH) &&
                completedAt != null &&
                completedAt.isAfter(LocalDateTime.now().minusHours(24)); // Same day only
    }
}
