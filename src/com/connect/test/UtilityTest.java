package com.connect.test;

import com.connect.util.DatabaseConnection;
import com.connect.util.IdGenerator;
import com.connect.util.PasswordUtil;
import com.connect.util.ValidationUtil;

import java.sql.Connection;

/**
 * Test class for all utility classes.
 * Run this to verify Phase 1 is working correctly.
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class UtilityTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 CONNECT PLATFORM - UTILITY CLASSES TEST");
        System.out.println("=".repeat(60));
        System.out.println();
        
        testDatabaseConnection();
        System.out.println();
        
        testIdGenerator();
        System.out.println();
        
        testPasswordUtil();
        System.out.println();
        
        testValidationUtil();
        System.out.println();
        
        System.out.println("=".repeat(60));
        System.out.println("✅ ALL TESTS COMPLETED!");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Test DatabaseConnection class
     */
    private static void testDatabaseConnection() {
        System.out.println("📊 Testing DatabaseConnection...");
        System.out.println("-".repeat(60));
        
        try {
            // Test connection
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection established successfully!");
                System.out.println("   Database: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   Version: " + conn.getMetaData().getDatabaseProductVersion());
            } else {
                System.out.println("❌ Connection failed!");
            }
            
            // Test connection test method
            boolean testResult = DatabaseConnection.testConnection();
            System.out.println("✅ Connection test method: " + (testResult ? "PASSED" : "FAILED"));
            
        } catch (Exception e) {
            System.out.println("❌ Database connection test failed!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("\n⚠️  TROUBLESHOOTING:");
            System.out.println("   1. Make sure XAMPP is running");
            System.out.println("   2. Check if 'connect_db' database exists in phpMyAdmin");
            System.out.println("   3. Verify MySQL credentials in DatabaseConnection.java");
            System.out.println("   4. Ensure mysql-connector-java is in classpath");
        }
    }
    
    /**
     * Test IdGenerator class
     */
    private static void testIdGenerator() {
        System.out.println("🔢 Testing IdGenerator...");
        System.out.println("-".repeat(60));
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Test User ID generation
            String userId = IdGenerator.generateUserId();
            System.out.println("✅ Generated User ID: " + userId);
            System.out.println("   Valid format: " + IdGenerator.isValidIdFormat(userId, "USR"));
            
            // Test Event ID generation
            String eventId = IdGenerator.generateEventId();
            System.out.println("✅ Generated Event ID: " + eventId);
            System.out.println("   Valid format: " + IdGenerator.isValidIdFormat(eventId, "EVT"));
            
            // Test Registration ID generation
            String regId = IdGenerator.generateRegistrationId();
            System.out.println("✅ Generated Registration ID: " + regId);
            System.out.println("   Valid format: " + IdGenerator.isValidIdFormat(regId, "REG"));
            
            // Test Certificate ID generation
            String certId = IdGenerator.generateCertificateId();
            System.out.println("✅ Generated Certificate ID: " + certId);
            
            // Test Review ID generation
            String reviewId = IdGenerator.generateReviewId();
            System.out.println("✅ Generated Review ID: " + reviewId);
            
        } catch (Exception e) {
            System.out.println("❌ ID generation test failed!");
            System.out.println("   Error: " + e.getMessage());
        }
    }
    
    /**
     * Test PasswordUtil class
     */
    private static void testPasswordUtil() {
        System.out.println("🔐 Testing PasswordUtil...");
        System.out.println("-".repeat(60));
        
        // Test password hashing
        String plainPassword = "TestPassword123";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        System.out.println("✅ Password hashed successfully!");
        System.out.println("   Plain: " + plainPassword);
        System.out.println("   Hashed: " + hashedPassword.substring(0, 30) + "...");
        
        // Test password verification (correct password)
        boolean verifyCorrect = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        System.out.println("✅ Correct password verification: " + 
                          (verifyCorrect ? "PASSED" : "FAILED"));
        
        // Test password verification (wrong password)
        boolean verifyWrong = PasswordUtil.verifyPassword("WrongPassword", hashedPassword);
        System.out.println("✅ Wrong password rejection: " + 
                          (!verifyWrong ? "PASSED" : "FAILED"));
        
        // Test password strength validation
        System.out.println("\n   Password Strength Tests:");
        testPasswordStrength("weak");           // Too short
        testPasswordStrength("weakpassword");   // No digit
        testPasswordStrength("12345678");       // No letter
        testPasswordStrength("StrongPass123");  // Valid
    }
    
    private static void testPasswordStrength(String password) {
        boolean isStrong = PasswordUtil.isPasswordStrong(password);
        String message = PasswordUtil.getPasswordStrengthMessage(password);
        System.out.println("   - \"" + password + "\": " + 
                          (isStrong ? "✅ Strong" : "❌ Weak") + 
                          " (" + message + ")");
    }
    
    /**
     * Test ValidationUtil class
     */
    private static void testValidationUtil() {
        System.out.println("✔️  Testing ValidationUtil...");
        System.out.println("-".repeat(60));
        
        // Test email validation
        System.out.println("   Email Validation:");
        testEmail("user@example.com");      // Valid
        testEmail("invalid.email");         // Invalid
        testEmail("test@domain");           // Invalid
        
        // Test phone validation
        System.out.println("\n   Phone Validation:");
        testPhone("+923001234567");         // Valid
        testPhone("03001234567");           // Valid
        testPhone("123");                   // Invalid
        
        // Test other validations
        System.out.println("\n   Other Validations:");
        System.out.println("   - Empty string: " + 
                          (!ValidationUtil.isNotEmpty("") ? "✅" : "❌"));
        System.out.println("   - Positive number (5): " + 
                          (ValidationUtil.isPositive(5) ? "✅" : "❌"));
        System.out.println("   - Negative number (-5): " + 
                          (!ValidationUtil.isPositive(-5) ? "✅" : "❌"));
        System.out.println("   - Valid capacity (100): " + 
                          (ValidationUtil.isValidCapacity(100) ? "✅" : "❌"));
        System.out.println("   - Invalid capacity (0): " + 
                          (!ValidationUtil.isValidCapacity(0) ? "✅" : "❌"));
        System.out.println("   - Valid rating (4): " + 
                          (ValidationUtil.isValidRating(4) ? "✅" : "❌"));
        System.out.println("   - Invalid rating (6): " + 
                          (!ValidationUtil.isValidRating(6) ? "✅" : "❌"));
    }
    
    private static void testEmail(String email) {
        boolean valid = ValidationUtil.isValidEmail(email);
        String error = ValidationUtil.getEmailError(email);
        System.out.println("   - \"" + email + "\": " + 
                          (valid ? "✅ Valid" : "❌ Invalid - " + error));
    }
    
    private static void testPhone(String phone) {
        boolean valid = ValidationUtil.isValidPhone(phone);
        String error = ValidationUtil.getPhoneError(phone);
        System.out.println("   - \"" + phone + "\": " + 
                          (valid ? "✅ Valid" : "❌ Invalid - " + error));
    }
}