package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTerminalRequest {

    @NotBlank(message = "Terminal ID is required")
    @Size(max = 50, message = "Terminal ID must not exceed 50 characters")
    private String terminalId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    private String manufacturer;

    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    private String serialNumber;

    @NotBlank(message = "Terminal type is required")
    @Pattern(regexp = "FIXED|PORTABLE|MOBILE|PINPAD|VIRTUAL",
             message = "Terminal type must be FIXED, PORTABLE, MOBILE, PINPAD, or VIRTUAL")
    private String terminalType;

    @NotBlank(message = "Connection type is required")
    @Pattern(regexp = "ETHERNET|WIFI|BLUETOOTH|SERIAL|GPRS|CLOUD_API",
             message = "Connection type must be ETHERNET, WIFI, BLUETOOTH, SERIAL, GPRS, or CLOUD_API")
    private String connectionType;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;

    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port;

    @Size(max = 20, message = "COM port must not exceed 20 characters")
    private String comPort;

    @Size(max = 50, message = "Merchant ID must not exceed 50 characters")
    private String merchantId;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    private Long cashRegisterId;

    private Boolean supportsContactless = false;
    private Boolean supportsChip = true;
    private Boolean supportsMagneticStripe = true;
    private Boolean supportsMobilePayment = false;

    @Size(max = 50, message = "Firmware version must not exceed 50 characters")
    private String firmwareVersion;

    private Boolean active = true;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
