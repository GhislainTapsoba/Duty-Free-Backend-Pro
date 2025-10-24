package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateLoyaltyCardRequest {
    @NotNull
    private Long customerId;
    
    private String tierLevel = "BRONZE";
    
    private LocalDate expiryDate;
}