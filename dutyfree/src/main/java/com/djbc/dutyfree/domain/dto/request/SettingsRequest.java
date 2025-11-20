package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {

    @NotBlank(message = "Setting key is required")
    private String key;

    private String value;

    private String category;

    private String description;
}
