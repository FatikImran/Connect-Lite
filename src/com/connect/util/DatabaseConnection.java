package com.connect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton pattern implementation for database connection management.
 * Enhanced with better error handling and troubleshooting.
 */
public class DatabaseConnection {
    
    private static Connection connection = null;
    
    // Database credentials - FIXED FOR XAMPP DEFAULT SETTINGS
    private static final String URL = "jdbc:mysql://192.168.100.93:3306/connect";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default is empty
    
    private DatabaseConnection() {
        // Prevent instantiation
    }
    
    /**
     * Gets the singleton database connection instance.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {
                System.out.println("🔌 Attempting database connection...");
                System.out.println("   URL: " + URL);
                System.out.println("   User: " + USER);
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connection established successfully!");
            }
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("💡 Add this to your pom.xml:");
            System.err.println("<dependency>");
            System.err.println("    <groupId>mysql</groupId>");
            System.err.println("    <artifactId>mysql-connector-java</artifactId>");
            System.err.println("    <version>8.0.33</version>");
            System.err.println("</dependency>");
            throw new SQLException("JDBC Driver not found", e);
        } catch (SQLException e) {
            handleConnectionError(e);
            throw e;
        }
    }
    
    /**
     * Detailed error handling for connection issues
     */
    private static void handleConnectionError(SQLException e) {
        System.err.println("❌ DATABASE CONNECTION FAILED!");
        System.err.println("   Error: " + e.getMessage());
        
        if (e.getMessage().contains("Communications link failure")) {
            System.err.println("\n🔧 TROUBLESHOOTING STEPS:");
            System.err.println("   1. 💻 Start XAMPP Control Panel");
            System.err.println("   2. 🟢 Click 'Start' next to MySQL");
            System.err.println("   3. 🌐 Open http://localhost/phpmyadmin");
            System.err.println("   4. 🗄  Create database: CREATE DATABASE connect;");
            System.err.println("   5. 📁 Run schema.sql to create tables");
        } else if (e.getMessage().contains("Unknown database")) {
            System.err.println("\n🔧 DATABASE DOESN'T EXIST:");
            System.err.println("   1. Open phpMyAdmin: http://localhost/phpmyadmin");
            System.err.println("   2. Click 'New' in left sidebar");
            System.err.println("   3. Enter 'connect' as database name");
            System.err.println("   4. Click 'Create'");
        } else if (e.getMessage().contains("Access denied")) {
            System.err.println("\n🔧 WRONG CREDENTIALS:");
            System.err.println("   Try password: 'root' or leave empty");
        }
    }
    
    /**
     * Enhanced connection test with detailed diagnostics
     */
    public static boolean testConnectionWithDiagnostics() {
        System.out.println("\n🔍 RUNNING DATABASE DIAGNOSTICS...");
        
        // Test 1: Check if MySQL service is running
        System.out.println("1. Testing MySQL service...");
        try {
            // Try to connect without specific database
            String testUrl = "jdbc:mysql://192.168.100.93:3306/?useSSL=false&serverTimezone=UTC";
            Connection testConn = DriverManager.getConnection(testUrl, USER, PASSWORD);
            System.out.println("   ✅ MySQL service is RUNNING");
            testConn.close();
        } catch (SQLException e) {
            System.err.println("   ❌ MySQL service is NOT RUNNING");
            System.err.println("   💡 Start MySQL in XAMPP Control Panel");
            return false;
        }
        
        // Test 2: Check if database exists
        System.out.println("2. Testing database 'connect'...");
        try {
            Connection conn = getConnection();
            System.out.println("   ✅ Database 'connect' is ACCESSIBLE");
            conn.close();
            return true;
        } catch (SQLException e) {
            System.err.println("   ❌ Database 'connect' not found or inaccessible");
            System.err.println("   💡 Create database in phpMyAdmin");
            return false;
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Quick connection test
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isConnected = conn != null && !conn.isClosed();
            if (isConnected) {
                System.out.println("✅ Database connection test: PASSED");
            }
            return isConnected;
        } catch (SQLException e) {
            System.err.println("❌ Database connection test: FAILED");
            return false;
        }
    }
}