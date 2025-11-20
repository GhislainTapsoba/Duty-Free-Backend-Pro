package com.djbc.dutyfree.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTerminalResponse {

    private Long id;
    private String terminalId;
    private String name;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String terminalType;
    private String connectionType;
    private String ipAddress;
    private Integer port;
    private String comPort;
    private String merchantId;
    private String location;

    private Long cashRegisterId;
    private String cashRegisterName;

    private Boolean supportsContactless;
    private Boolean supportsChip;
    private Boolean supportsMagneticStripe;
    private Boolean supportsMobilePayment;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastHeartbeat;

    private String firmwareVersion;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;
    private String notes;

    // Computed fields
    private Boolean ready; // If terminal is ready for transactions
    private Long secondsSinceLastHeartbeat;
}
