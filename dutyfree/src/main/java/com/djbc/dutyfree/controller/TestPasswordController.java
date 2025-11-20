package com.djbc.dutyfree.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestPasswordController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/generate-hash")
    public Map<String, Object> generateHash(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        String hash = passwordEncoder.encode(password);
        result.put("password", password);
        result.put("hash", hash);
        result.put("verification", passwordEncoder.matches(password, hash));

        // Test the existing hash
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        result.put("existingHashMatchesAdmin123", passwordEncoder.matches("admin123", existingHash));

        return result;
    }

    @GetMapping("/verify-hash")
    public Map<String, Object> verifyHash(@RequestParam String password, @RequestParam String hash) {
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", passwordEncoder.matches(password, hash));
        return result;
    }
}
