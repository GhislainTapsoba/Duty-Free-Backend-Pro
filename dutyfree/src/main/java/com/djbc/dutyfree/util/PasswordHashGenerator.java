package com.djbc.dutyfree.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("super123: " + encoder.encode("super123"));
        System.out.println("caisse123: " + encoder.encode("caisse123"));
        System.out.println("stock123: " + encoder.encode("stock123"));
    }
}