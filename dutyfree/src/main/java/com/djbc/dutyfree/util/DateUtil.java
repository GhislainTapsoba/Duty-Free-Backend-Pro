package com.djbc.dutyfree.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Get current date
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Get current date time
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Format date to string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Format date time to string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Format time to string
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    /**
     * Parse date from string
     */
    public static LocalDate parseDate(String dateString) {
        return dateString != null ? LocalDate.parse(dateString, DATE_FORMATTER) : null;
    }

    /**
     * Parse date time from string
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return dateTimeString != null ? LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER) : null;
    }

    /**
     * Get start of day
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Get end of day
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Get start of month
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Get end of month
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate hours between two date times
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    /**
     * Add days to date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * Add months to date
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    /**
     * Add years to date
     */
    public static LocalDate addYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    /**
     * Get age from birth date
     */
    public static int getAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}