package com.djbc.dutyfree.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.regex.Pattern;

@Component
@Slf4j
public class BarcodeUtil {
    
    private static final Pattern EAN13_PATTERN = Pattern.compile("^\\d{13}$");
    private static final Pattern EAN8_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern UPC_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Random RANDOM = new Random();
    
    /**
     * Validate EAN-13 barcode
     */
    public boolean isValidEAN13(String barcode) {
        if (barcode == null || !EAN13_PATTERN.matcher(barcode).matches()) {
            return false;
        }
        
        return calculateEAN13CheckDigit(barcode.substring(0, 12)) == 
                Character.getNumericValue(barcode.charAt(12));
    }
    
    /**
     * Validate EAN-8 barcode
     */
    public boolean isValidEAN8(String barcode) {
        if (barcode == null || !EAN8_PATTERN.matcher(barcode).matches()) {
            return false;
        }
        
        return calculateEAN8CheckDigit(barcode.substring(0, 7)) == 
                Character.getNumericValue(barcode.charAt(7));
    }
    
    /**
     * Validate UPC barcode
     */
    public boolean isValidUPC(String barcode) {
        if (barcode == null || !UPC_PATTERN.matcher(barcode).matches()) {
            return false;
        }
        
        return calculateUPCCheckDigit(barcode.substring(0, 11)) == 
                Character.getNumericValue(barcode.charAt(11));
    }
    
    /**
     * Generate EAN-13 barcode
     */
    public String generateEAN13() {
        StringBuilder barcode = new StringBuilder();
        
        // Country code (example: 611 for Burkina Faso)
        barcode.append("611");
        
        // Manufacturer code (5 digits)
        for (int i = 0; i < 5; i++) {
            barcode.append(RANDOM.nextInt(10));
        }
        
        // Product code (4 digits)
        for (int i = 0; i < 4; i++) {
            barcode.append(RANDOM.nextInt(10));
        }
        
        // Calculate and append check digit
        int checkDigit = calculateEAN13CheckDigit(barcode.toString());
        barcode.append(checkDigit);
        
        return barcode.toString();
    }
    
    /**
     * Generate internal barcode for products
     */
    public String generateInternalBarcode(String prefix) {
        StringBuilder barcode = new StringBuilder(prefix);
        
        // Add timestamp component
        long timestamp = System.currentTimeMillis() % 1000000;
        barcode.append(String.format("%06d", timestamp));
        
        // Add random component
        int random = RANDOM.nextInt(1000);
        barcode.append(String.format("%03d", random));
        
        return barcode.toString();
    }
    
    /**
     * Calculate EAN-13 check digit
     */
    private int calculateEAN13CheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : 10 - remainder;
    }
    
    /**
     * Calculate EAN-8 check digit
     */
    private int calculateEAN8CheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : 10 - remainder;
    }
    
    /**
     * Calculate UPC check digit
     */
    private int calculateUPCCheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : 10 - remainder;
    }
    
    /**
     * Format barcode for display
     */
    public String formatBarcode(String barcode) {
        if (barcode == null) return null;
        
        if (barcode.length() == 13) {
            // Format EAN-13: XXX-XXXXX-XXXX-X
            return String.format("%s-%s-%s-%s",
                    barcode.substring(0, 3),
                    barcode.substring(3, 8),
                    barcode.substring(8, 12),
                    barcode.substring(12));
        } else if (barcode.length() == 12) {
            // Format UPC: XXX-XXX-XXX-XXX
            return String.format("%s-%s-%s-%s",
                    barcode.substring(0, 3),
                    barcode.substring(3, 6),
                    barcode.substring(6, 9),
                    barcode.substring(9));
        }
        
        return barcode;
    }
}