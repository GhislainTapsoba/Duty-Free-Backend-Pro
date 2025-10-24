package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateWastageRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private LocalDate wastageDate;

    @NotNull
    private String reason; // EXPIRED, DAMAGED, THEFT, OTHER

    private String description;
}