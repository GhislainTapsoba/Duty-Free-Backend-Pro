package com.djbc.dutyfree.domain.dto.request;

import com.djbc.dutyfree.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String password; // Optional for updates

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    private Long cashRegisterId;

    private Boolean active;
}
