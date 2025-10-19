package com.djbc.dutyfree.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

    private static final Locale LOCALE_FR = Locale.FRANCE;
    private static final int DECIMAL_PLACES = 2;

    /**
     * Format amount as currency
     */
    public static String formatXOF(BigDecimal amount) {
        if (amount == null) return "0 FCFA";
        NumberFormat formatter = NumberFormat.getNumberInstance(LOCALE_FR);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount) + " FCFA";
    }

    /**
     * Format amount as EUR
     */
    public static String formatEUR(BigDecimal amount) {
        if (amount == null) return "€0.00";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_FR);
        return formatter.format(amount);
    }

    /**
     * Format amount as USD
     */
    public static String formatUSD(BigDecimal amount) {
        if (amount == null) return "$0.00";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }

    /**
     * Round to 2 decimal places
     */
    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     * Calculate percentage
     */
    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     * Calculate discount
     */
    public static BigDecimal applyDiscount(BigDecimal amount, BigDecimal discountPercentage) {
        BigDecimal discount = calculatePercentage(amount, discountPercentage);
        return amount.subtract(discount);
    }

    /**
     * Calculate tax
     */
    public static BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
        return calculatePercentage(amount, taxRate);
    }

    /**
     * Add tax to amount
     */
    public static BigDecimal addTax(BigDecimal amount, BigDecimal taxRate) {
        BigDecimal tax = calculateTax(amount, taxRate);
        return amount.add(tax);
    }

    /**
     * Calculate amount including tax
     */
    public static BigDecimal calculateAmountWithTax(BigDecimal amountExcludingTax, BigDecimal taxRate) {
        return addTax(amountExcludingTax, taxRate);
    }

    /**
     * Calculate amount excluding tax
     */
    public static BigDecimal calculateAmountWithoutTax(BigDecimal amountIncludingTax, BigDecimal taxRate) {
        BigDecimal divisor = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return amountIncludingTax.divide(divisor, DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     * Compare amounts (for sorting)
     */
    public static int compare(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null && amount2 == null) return 0;
        if (amount1 == null) return -1;
        if (amount2 == null) return 1;
        return amount1.compareTo(amount2);
    }

    /**
     * Check if amount is positive
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if amount is zero
     */
    public static boolean isZero(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Get the maximum of two amounts
     */
    public static BigDecimal max(BigDecimal amount1, BigDecimal amount2) {
        return amount1.max(amount2);
    }

    /**
     * Get the minimum of two amounts
     */
    public static BigDecimal min(BigDecimal amount1, BigDecimal amount2) {
        return amount1.min(amount2);
    }
}