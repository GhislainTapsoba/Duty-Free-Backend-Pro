package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.LoginRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.AuthResponse;
import com.djbc.dutyfree.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user information")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        var user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless, logout is handled on client side
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}