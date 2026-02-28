package com.connect.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Utility class for input validation across the application.
 * Centralizes validation logic to maintain consistency.
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class ValidationUtil {
    
    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{10,15}$"
    );
    
    // Name pattern: Letters, spaces, hyphens, apostrophes (2-100 chars)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z][A-Za-z\\s'-]{1,99}$"
    );
    
    // Password pattern: At least 8 chars, must contain letters and numbers
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"
    );
    
    /**
     * Validates email format.
     * 
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates phone number format.
     * Accepts formats: +923001234567, 03001234567, etc.
     * 
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validates name format.
     * Must start with a letter, can contain letters, spaces, hyphens, apostrophes.
     * Length: 2-100 characters.
     * 
     * Examples of valid names:
     * - "Muhammad Fatik"
     * - "O'Connor"
     * - "Jean-Pierre"
     * 
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validates password strength.
     * Requirements:
     * - Minimum 8 characters
     * - Must contain at least one letter
     * - Must contain at least one number
     * - Can contain special characters: @$!%*#?&
     * 
     * Examples of valid passwords:
     * - "password123"
     * - "MyPass123"
     * - "Secure@123"
     * 
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param value String to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validates that an integer is positive.
     * 
     * @param value Integer to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * Validates that end date is after start date.
     * 
     * @param start Start date/time
     * @param end End date/time
     * @return true if end is after start, false otherwise
     */
    public static boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        return end.isAfter(start);
    }
    
    /**
     * Validates that a date is in the future.
     * 
     * @param dateTime Date/time to validate
     * @return true if in future, false otherwise
     */
    public static boolean isInFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Validates event capacity.
     * 
     * @param capacity Event capacity
     * @return true if valid (> 0 and <= 10000), false otherwise
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0 && capacity <= 10000;
    }
    
    /**
     * Validates rating value (1-5).
     * 
     * @param rating Rating value
     * @return true if valid (1-5), false otherwise
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }
    
    /**
     * Validates string length.
     * 
     * @param value String to validate
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @return true if within range, false otherwise
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Gets validation error message for email.
     * 
     * @param email Email to validate
     * @return Error message or null if valid
     */
    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        return null;
    }
    
    /**
     * Gets validation error message for phone.
     * 
     * @param phone Phone to validate
     * @return Error message or null if valid
     */
    public static String getPhoneError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number cannot be empty";
        }
        if (!isValidPhone(phone)) {
            return "Invalid phone format (use +923001234567 or 03001234567)";
        }
        return null;
    }
    
    /**
     * Gets validation error message for name.
     * 
     * @param name Name to validate
     * @return Error message or null if valid
     */
    public static String getNameError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty";
        }
        if (name.trim().length() < 2) {
            return "Name must be at least 2 characters";
        }
        if (name.trim().length() > 100) {
            return "Name must be less than 100 characters";
        }
        if (!isValidName(name)) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }
        return null;
    }
    
    /**
     * Gets validation error message for password.
     * 
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Za-z].*")) {
            return "Password must contain at least one letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }
        if (!isValidPassword(password)) {
            return "Password contains invalid characters";
        }
        return null;
    }
}