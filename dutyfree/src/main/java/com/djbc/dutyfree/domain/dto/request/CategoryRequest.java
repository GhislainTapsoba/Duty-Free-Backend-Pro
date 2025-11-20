package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "French name is required")
    private String nameFr;

    @NotBlank(message = "English name is required")
    private String nameEn;

    private String descriptionFr;
    private String descriptionEn;

    private Boolean active;
}
