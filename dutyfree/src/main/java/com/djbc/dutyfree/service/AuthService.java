package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.LoginRequest;
import com.djbc.dutyfree.domain.dto.response.AuthResponse;
import com.djbc.dutyfree.domain.entity.User;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.UserRepository;
import com.djbc.dutyfree.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Log pour debug
            log.debug("Attempting login for user: {}", request.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("User {} logged in successfully", user.getUsername());
            log.info("Login request received for username: {}", request.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .cashRegisterId(user.getCashRegisterId())  // ✅ CHANGEMENT ICI : utilise la nouvelle méthode
                    .build();

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getUsername());
            throw new BadRequestException("Invalid username or password");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            log.error("User not found: {}", request.getUsername());
            throw new BadRequestException("Invalid username or password");
        } catch (Exception e) {
            log.error("Login failed for user: {} - Error: {}", request.getUsername(), e.getMessage(), e);
            throw new BadRequestException("Login failed: " + e.getMessage());
        }
    }

    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                throw new BadRequestException("No authenticated user found");
            }
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Initialize lazy fields to avoid serialization issues
            if (user.getAssignedCashRegister() != null) {
                user.getAssignedCashRegister().getId(); // Force initialization
            }
            
            return user;
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            throw new BadRequestException("Unable to get current user: " + e.getMessage());
        }
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCurrentUserDto() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                throw new BadRequestException("No authenticated user found");
            }
            String username = authentication.getName();
            
            // Use a direct query to avoid lazy loading issues
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new ResourceNotFoundException("User not found");
            }
            
            User user = userOpt.get();
            
            // Create a safe DTO manually without accessing lazy fields
            var userMap = new java.util.HashMap<String, Object>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("fullName", user.getFullName());
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
            userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
            userMap.put("role", user.getRole().toString());
            userMap.put("active", user.getActive());
            userMap.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            userMap.put("cashRegisterId", user.getCashRegisterId()); // ✅ CHANGEMENT ICI : utilise la nouvelle méthode
            
            return userMap;
        } catch (Exception e) {
            log.error("Error getting current user DTO: {}", e.getMessage(), e);
            throw new BadRequestException("Unable to get current user: " + e.getMessage());
        }
    }
}