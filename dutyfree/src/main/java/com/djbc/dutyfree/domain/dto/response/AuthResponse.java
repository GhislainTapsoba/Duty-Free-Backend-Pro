package com.djbc.dutyfree.domain.dto.response;

import com.djbc.dutyfree.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String fullName;
    private Role role;
    private Long cashRegisterId;
}