package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalSheetRequest {

    @NotBlank(message = "Sheet code is required")
    @Size(max = 50, message = "Sheet code must not exceed 50 characters")
    private String sheetCode;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Long productId;

    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String version = "1.0";

    @NotNull(message = "Output quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Output quantity must be greater than 0")
    @Digits(integer = 16, fraction = 3, message = "Invalid output quantity format")
    private BigDecimal outputQuantity = BigDecimal.ONE;

    @NotBlank(message = "Output unit is required")
    @Size(max = 20, message = "Output unit must not exceed 20 characters")
    private String outputUnit = "PIECE";

    @Min(value = 0, message = "Preparation time must be 0 or greater")
    private Integer preparationTime;

    @Min(value = 0, message = "Cooking time must be 0 or greater")
    private Integer cookingTime;

    @Size(max = 20, message = "Difficulty must not exceed 20 characters")
    private String difficulty = "MEDIUM";

    @Size(max = 5000, message = "Instructions must not exceed 5000 characters")
    private String instructions;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private Boolean active = true;

    private Boolean validated = false;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<TechnicalSheetItemRequest> items = new ArrayList<>();
}
