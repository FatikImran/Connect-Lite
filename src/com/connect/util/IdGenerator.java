package com.connect.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class for generating unique IDs with specific prefixes.
 * FIXED VERSION - Does not close singleton connection
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class IdGenerator {
    
    /**
     * Generates a unique User ID in format: USR001, USR002, etc.
     */
    public static String generateUserId() throws SQLException {
        System.out.println("🔍 [IdGenerator] generateUserId() called");
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("   Database connection obtained");
            String id = generateId(conn, "users", "userId", "USR");
            System.out.println("   Generated ID: " + id);
            return id;
        } catch (SQLException e) {
            System.err.println("❌ [IdGenerator] Failed to generate User ID");
            System.err.println("   Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generates a unique Event ID in format: EVT001, EVT002, etc.
     */
    public static String generateEventId() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        return generateId(conn, "Events", "eventId", "EVT");
    }
    
    /**
     * Generates a unique Registration ID in format: REG001, REG002, etc.
     */
    public static String generateRegistrationId() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        return generateId(conn, "Registrations", "registrationId", "REG");
    }
    
    /**
     * Generates a unique Certificate ID in format: CRT001, CRT002, etc.
     */
    public static String generateCertificateId() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        return generateId(conn, "Certificates", "certificateId", "CRT");
    }
    
    /**
     * Generates a unique Review ID in format: REV001, REV002, etc.
     */
    public static String generateReviewId() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        return generateId(conn, "Reviews", "reviewId", "REV");
    }
    
    /**
     * Generic method to generate IDs for any table.
     * IMPORTANT: Does NOT close connection (singleton pattern)
     */
    private static String generateId(Connection conn, String tableName, 
                                     String columnName, String prefix) throws SQLException {
        System.out.println("🔍 [IdGenerator] generateId() for " + tableName);
        System.out.println("   Table: " + tableName);
        System.out.println("   Column: " + columnName);
        System.out.println("   Prefix: " + prefix);
        
        String query = String.format(
            "SELECT %s FROM %s WHERE %s LIKE ? ORDER BY %s DESC LIMIT 1",
            columnName, tableName, columnName, columnName
        );
        
        System.out.println("   Query: " + query);
        System.out.println("   Search pattern: " + prefix + "%");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, prefix + "%");
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                // Extract the numeric part from the last ID
                String lastId = rs.getString(columnName);
                System.out.println("   Last ID found: " + lastId);
                
                String numericPart = lastId.substring(prefix.length());
                System.out.println("   Numeric part: " + numericPart);
                
                int lastNumber = Integer.parseInt(numericPart);
                System.out.println("   Last number: " + lastNumber);
                
                // Increment and format with leading zeros (3 digits)
                int newNumber = lastNumber + 1;
                String newId = String.format("%s%03d", prefix, newNumber);
                System.out.println("   New ID generated: " + newId);
                
                return newId;
            } else {
                // No existing IDs, start with 001
                String newId = prefix + "001";
                System.out.println("   No existing IDs found");
                System.out.println("   Starting with: " + newId);
                return newId;
            }
        } catch (Exception e) {
            System.err.println("❌ [IdGenerator] Error generating ID for " + tableName);
            System.err.println("   Exception: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Failed to generate ID", e);
        } finally {
            // Close ResultSet and PreparedStatement, but NOT the connection
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("⚠ Error closing ResultSet: " + e.getMessage());
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("⚠ Error closing PreparedStatement: " + e.getMessage());
                }
            }
            // DO NOT close connection - it's a singleton!
        }
    }
    
    /**
     * Validates if an ID follows the correct format (PREFIX + 3 digits).
     */
    public static boolean isValidIdFormat(String id, String prefix) {
        if (id == null || id.length() != prefix.length() + 3) {
            return false;
        }
        
        if (!id.startsWith(prefix)) {
            return false;
        }
        
        String numericPart = id.substring(prefix.length());
        try {
            Integer.parseInt(numericPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}