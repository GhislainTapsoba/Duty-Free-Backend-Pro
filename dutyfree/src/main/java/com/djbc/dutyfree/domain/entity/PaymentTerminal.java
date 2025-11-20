package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a Payment Terminal (TPE)
 * Supports various terminal types and manufacturers
 */
@Entity
@Table(name = "payment_terminals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false, unique = true, length = 50)
    private String terminalId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "manufacturer", length = 50)
    private String manufacturer; // INGENICO, VERIFONE, PAX, etc.

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "terminal_type", nullable = false, length = 20)
    private TerminalType terminalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 20)
    private ConnectionType connectionType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "com_port", length = 20)
    private String comPort;

    @Column(name = "merchant_id", length = 50)
    private String merchantId;

    @Column(name = "location", length = 100)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id")
    private CashRegister cashRegister;

    @Column(name = "supports_contactless")
    private Boolean supportsContactless = false;

    @Column(name = "supports_chip")
    private Boolean supportsChip = true;

    @Column(name = "supports_magnetic_stripe")
    private Boolean supportsMagneticStripe = true;

    @Column(name = "supports_mobile_payment")
    private Boolean supportsMobilePayment = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TerminalStatus status = TerminalStatus.OFFLINE;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (deleted == null) {
            deleted = false;
        }
        if (status == null) {
            status = TerminalStatus.OFFLINE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TerminalType {
        FIXED,          // Terminal fixe
        PORTABLE,       // Terminal portable
        MOBILE,         // Terminal mobile
        PINPAD,         // PIN pad only
        VIRTUAL         // Terminal virtuel (pour tests)
    }

    public enum ConnectionType {
        ETHERNET,       // Connexion réseau
        WIFI,           // WiFi
        BLUETOOTH,      // Bluetooth
        SERIAL,         // Port série (RS232/USB)
        GPRS,           // GPRS/3G/4G
        CLOUD_API       // API Cloud
    }

    public enum TerminalStatus {
        ONLINE,         // En ligne et prêt
        OFFLINE,        // Hors ligne
        BUSY,           // Occupé (transaction en cours)
        ERROR,          // En erreur
        MAINTENANCE     // En maintenance
    }

    /**
     * Check if terminal is ready for transaction
     */
    public boolean isReady() {
        return active && !deleted && status == TerminalStatus.ONLINE;
    }

    /**
     * Check if terminal supports a specific payment method
     */
    public boolean supportsPaymentMethod(String method) {
        return switch (method.toUpperCase()) {
            case "CONTACTLESS", "NFC" -> supportsContactless;
            case "CHIP", "EMV" -> supportsChip;
            case "MAGNETIC", "SWIPE" -> supportsMagneticStripe;
            case "MOBILE", "APPLE_PAY", "GOOGLE_PAY" -> supportsMobilePayment;
            default -> false;
        };
    }
}
