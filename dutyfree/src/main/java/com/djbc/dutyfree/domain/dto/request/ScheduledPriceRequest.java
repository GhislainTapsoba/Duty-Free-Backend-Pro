package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPriceRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Price type is required")
    @Pattern(regexp = "FIXED|DISCOUNT|MARKUP", message = "Price type must be FIXED, DISCOUNT, or MARKUP")
    private String priceType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be 0 or greater")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Invalid percentage format")
    private BigDecimal percentage;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "XOF";

    private LocalDate validFrom;

    private LocalDate validUntil;

    private LocalTime timeFrom;

    private LocalTime timeUntil;

    @Size(max = 100, message = "Days of week must not exceed 100 characters")
    private String daysOfWeek;

    @Min(value = 0, message = "Priority must be 0 or greater")
    private Integer priority = 0;

    @Pattern(regexp = "DAILY|WEEKLY|SEASONAL|PROMOTIONAL|SPECIAL_EVENT",
             message = "Period type must be DAILY, WEEKLY, SEASONAL, PROMOTIONAL, or SPECIAL_EVENT")
    private String periodType = "PROMOTIONAL";

    private Boolean active = true;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
