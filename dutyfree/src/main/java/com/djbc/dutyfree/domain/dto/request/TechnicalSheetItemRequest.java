package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheetItemRequest {

    private Long id; // For updates

    @NotNull(message = "Raw material ID is required")
    private Long rawMaterialId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    @Digits(integer = 16, fraction = 3, message = "Invalid quantity format")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @Min(value = 0, message = "Display order must be 0 or greater")
    private Integer displayOrder = 0;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private Boolean optional = false;

    @DecimalMin(value = "0.0", inclusive = false, message = "Conversion factor must be greater than 0")
    @Digits(integer = 16, fraction = 6, message = "Invalid conversion factor format")
    private BigDecimal conversionFactor = BigDecimal.ONE;
}
