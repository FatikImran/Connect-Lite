package com.connect.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with salt for secure password storage.
 * 
 * Security Note: In production, consider using BCrypt or Argon2.
 * For this semester project, SHA-256 with salt is sufficient.
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class PasswordUtil {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    
    /**
     * Hashes a plain text password with a random salt.
     * Returns the salt and hash combined in format: salt:hash
     * 
     * @param plainPassword The plain text password
     * @return Salted hash in format "salt:hash" (Base64 encoded)
     * @throws RuntimeException if hashing fails
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            byte[] hash = hashWithSalt(plainPassword, salt);
            
            // Encode both salt and hash to Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            // Return combined: salt:hash
            return saltBase64 + ":" + hashBase64;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verifies if a plain password matches the stored hashed password.
     * 
     * @param plainPassword The plain text password to verify
     * @param storedPassword The stored password (salt:hash format)
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        try {
            // Split stored password into salt and hash
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            // Decode salt and hash from Base64
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the plain password with the same salt
            byte[] testHash = hashWithSalt(plainPassword, salt);
            
            // Compare the hashes
            return MessageDigest.isEqual(storedHash, testHash);
            
        } catch (Exception e) {
            System.err.println("❌ Password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hashes a password with a given salt using SHA-256.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password as byte array
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) 
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt); // Add salt first
        byte[] hashedPassword = md.digest(password.getBytes());
        return hashedPassword;
    }
    
    /**
     * Validates password strength (basic validation for semester project).
     * 
     * Password requirements:
     * - At least 8 characters
     * - Contains at least one digit
     * - Contains at least one letter
     * 
     * @param password The password to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        
        return hasDigit && hasLetter;
    }
    
    /**
     * Gets password strength message for user feedback.
     * 
     * @param password The password to check
     * @return Descriptive message about password strength
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        
        if (!password.matches(".*[a-zA-Z].*")) {
            return "Password must contain at least one letter";
        }
        
        return "Password is strong";
    }
}