package com.connect.test;

import java.sql.*;

import com.connect.util.DatabaseConnection;
import com.connect.util.IdGenerator;
import com.connect.util.PasswordUtil;

/**
 * Utility to test database connection and schema
 * Run this BEFORE trying to sign up
 */
public class DatabaseTester {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  DATABASE CONNECTION & SCHEMA TEST");
        System.out.println("========================================\n");
        
        testConnection();
        testUsersTable();
        testAllTables();
        testUserCreation();
        
        System.out.println("\n========================================");
        System.out.println("  TEST COMPLETE");
        System.out.println("========================================");
    }
    
    /**
     * Test 1: Database Connection
     */
    private static void testConnection() {
        System.out.println("TEST 1: Database Connection");
        System.out.println("----------------------------");
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful!");
                System.out.println("   Connection URL: jdbc:mysql://localhost:3306/connect");
                System.out.println("   Database: " + conn.getCatalog());
                
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("   MySQL Version: " + meta.getDatabaseProductVersion());
                System.out.println("   Driver: " + meta.getDriverName());
            } else {
                System.err.println("❌ Connection is null or closed");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("\n💡 TROUBLESHOOTING:");
            System.err.println("   1. Is XAMPP running?");
            System.err.println("   2. Is MySQL service started in XAMPP?");
            System.err.println("   3. Does database 'connect' exist?");
            System.err.println("   4. Run this SQL in phpMyAdmin: CREATE DATABASE IF NOT EXISTS connect;");
        }
        System.out.println();
    }
    
    /**
     * Test 2: Users Table Exists
     */
    private static void testUsersTable() {
        System.out.println("TEST 2: Users Table");
        System.out.println("-------------------");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Check if Users table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Users", null);
            
            if (tables.next()) {
                System.out.println("✅ Users table exists");
                
                // Get column information
                ResultSet columns = meta.getColumns(null, null, "Users", null);
                System.out.println("\n   Columns:");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    System.out.println("   - " + columnName + " (" + columnType + 
                                     (columnSize > 0 ? ", " + columnSize : "") + ")");
                }
                
                // Check row count
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("\n   Current records: " + count);
                    }
                }
                
            } else {
                System.err.println("❌ Users table does NOT exist!");
                System.err.println("\n💡 SOLUTION:");
                System.err.println("   Run the schema.sql file in phpMyAdmin to create tables");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking Users table: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test 3: Check All Tables
     */
    private static void testAllTables() {
        System.out.println("TEST 3: All Required Tables");
        System.out.println("---------------------------");
        
        String[] requiredTables = {
            "Users", "Events", "Registrations", 
            "Certificates", "Reviews", "User_Interests"
        };
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            for (String tableName : requiredTables) {
                ResultSet tables = meta.getTables(null, null, tableName, null);
                if (tables.next()) {
                    System.out.println("✅ " + tableName + " table exists");
                } else {
                    System.err.println("❌ " + tableName + " table MISSING");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking tables: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test 4: Test User Creation (Dry Run)
     */
    private static void testUserCreation() {
        System.out.println("TEST 4: User Creation Test (Dry Run)");
        System.out.println("------------------------------------");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Test ID generation
            System.out.println("Testing ID generation...");
            String testId = IdGenerator.generateUserId();
            System.out.println("✅ Generated User ID: " + testId);
            
            // Test password hashing
            System.out.println("\nTesting password hashing...");
            String testPassword = "test123456";
            String hashedPassword = PasswordUtil.hashPassword(testPassword);
            System.out.println("✅ Password hashed successfully");
            System.out.println("   Original length: " + testPassword.length());
            System.out.println("   Hashed length: " + hashedPassword.length());
            
            // Test password verification
            boolean verified = PasswordUtil.verifyPassword(testPassword, hashedPassword);
            System.out.println("✅ Password verification: " + (verified ? "PASSED" : "FAILED"));
            
            // Test email existence check (should be false)
            System.out.println("\nTesting email existence check...");
            String testEmail = "test@example.com";
            
            String checkSql = "SELECT COUNT(*) FROM Users WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, testEmail);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✅ Email check query works");
                    System.out.println("   Email '" + testEmail + "' exists: " + (count > 0));
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Quick method to check if database is ready
     */
    public static boolean isDatabaseReady() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if Users table exists and is accessible
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users")) {
                rs.next();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }
}