package com.djbc.dutyfree.controller.test;

import com.djbc.dutyfree.domain.entity.User;
import com.djbc.dutyfree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/check-auth")
    public Map<String, Object> checkAuth(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            response.put("userExists", user != null);
            
            if (user != null) {
                response.put("username", user.getUsername());
                response.put("active", user.getActive());
                response.put("role", user.getRole().toString());
                response.put("passwordHashInDB", user.getPassword());
                
                boolean matches = passwordEncoder.matches(password, user.getPassword());
                response.put("passwordMatches", matches);
                
                String newHash = passwordEncoder.encode(password);
                response.put("newHashGenerated", newHash);
                response.put("newHashVerifies", passwordEncoder.matches(password, newHash));
            } else {
                response.put("error", "User not found in database");
            }
        } catch (Exception e) {
            response.put("exception", e.getMessage());
            response.put("exceptionClass", e.getClass().getName());
        }
        
        return response;
    }
}