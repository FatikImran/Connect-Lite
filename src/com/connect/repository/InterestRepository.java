package com.connect.repository;

import com.connect.model.Interest;
import com.connect.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Interest data access.
 * Handles all database operations for User_Interests table.
 * 
 * Note: Simple design with composite primary key (userId, interestName).
 * No separate interest categories or interest IDs.
 * 
 * Design Pattern: Repository Pattern
 * GRASP Principle: Information Expert (knows how to access interest data)
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class InterestRepository {

    /**
     * Adds a new interest for a user.
     * 
     * @param interest Interest object to add
     * @return true if addition successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean addInterest(Interest interest) throws SQLException {
        String sql = "INSERT INTO User_Interests (userId, interestName) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, interest.getUserId());
            ps.setString(2, interest.getInterestName());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Check for duplicate entry
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("❌ Interest already exists for this user");
            } else {
                System.err.println("❌ Error adding interest: " + e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Adds multiple interests for a user in a single transaction.
     * More efficient than calling addInterest() multiple times.
     * 
     * @param userId The user ID
     * @param interestNames List of interest names to add
     * @return Number of interests successfully added
     * @throws SQLException if database operation fails
     */
    public int addMultipleInterests(String userId, List<String> interestNames) throws SQLException {
        String sql = "INSERT INTO User_Interests (userId, interestName) VALUES (?, ?)";
        int successCount = 0;
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            ps = conn.prepareStatement(sql);
            
            for (String interestName : interestNames) {
                ps.setString(1, userId);
                ps.setString(2, interestName);
                
                try {
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        successCount++;
                    }
                } catch (SQLException e) {
                    // Skip duplicates, continue with others
                    if (!e.getMessage().contains("Duplicate entry")) {
                        throw e;
                    }
                }
            }
            
            conn.commit(); // Commit transaction
            return successCount;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Error adding multiple interests: " + e.getMessage());
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("❌ Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    System.err.println("❌ Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Removes an interest for a user.
     * 
     * @param interest Interest object to remove
     * @return true if removal successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean removeInterest(Interest interest) throws SQLException {
        String sql = "DELETE FROM User_Interests WHERE userId = ? AND interestName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, interest.getUserId());
            ps.setString(2, interest.getInterestName());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error removing interest: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Removes an interest by userId and interestName.
     * 
     * @param userId The user ID
     * @param interestName The interest name
     * @return true if removal successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean removeInterest(String userId, String interestName) throws SQLException {
        return removeInterest(new Interest(userId, interestName));
    }

    /**
     * Removes all interests for a specific user.
     * Useful when user wants to reset their interests.
     * 
     * @param userId The user ID
     * @return Number of interests removed
     * @throws SQLException if database operation fails
     */
    public int removeAllForUser(String userId) throws SQLException {
        String sql = "DELETE FROM User_Interests WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected;
            
        } catch (SQLException e) {
            System.err.println("❌ Error removing all interests for user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all interests for a specific user.
     * Used for displaying user's interest profile.
     * 
     * @param userId The user ID
     * @return List of interests for that user
     * @throws SQLException if database operation fails
     */
    public List<Interest> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM User_Interests WHERE userId = ? ORDER BY interestName ASC";
        List<Interest> interests = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interests.add(mapResultSetToInterest(rs));
                }
            }
            
            return interests;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding interests by user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all interest names for a specific user (without Interest objects).
     * Returns just the list of interest names as strings.
     * 
     * @param userId The user ID
     * @return List of interest names
     * @throws SQLException if database operation fails
     */
    public List<String> findInterestNamesByUserId(String userId) throws SQLException {
        String sql = "SELECT interestName FROM User_Interests WHERE userId = ? " +
                     "ORDER BY interestName ASC";
        List<String> interestNames = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interestNames.add(rs.getString("interestName"));
                }
            }
            
            return interestNames;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding interest names by user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all users who have a specific interest.
     * Useful for event recommendations and targeted notifications.
     * 
     * @param interestName The interest name
     * @return List of user IDs who have this interest
     * @throws SQLException if database operation fails
     */
    public List<String> findUsersByInterest(String interestName) throws SQLException {
        String sql = "SELECT userId FROM User_Interests WHERE interestName = ? ORDER BY userId ASC";
        List<String> userIds = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, interestName);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getString("userId"));
                }
            }
            
            return userIds;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding users by interest: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all users who have ANY of the specified interests.
     * Useful for event recommendations based on multiple categories.
     * 
     * @param interestNames List of interest names
     * @return List of user IDs who have at least one of these interests
     * @throws SQLException if database operation fails
     */
    public List<String> findUsersByAnyInterest(List<String> interestNames) throws SQLException {
        if (interestNames == null || interestNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Build dynamic SQL with IN clause
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT DISTINCT userId FROM User_Interests WHERE interestName IN ("
        );
        
        for (int i = 0; i < interestNames.size(); i++) {
            sqlBuilder.append("?");
            if (i < interestNames.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(") ORDER BY userId ASC");
        
        String sql = sqlBuilder.toString();
        List<String> userIds = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < interestNames.size(); i++) {
                ps.setString(i + 1, interestNames.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getString("userId"));
                }
            }
            
            return userIds;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding users by any interest: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all users who have ALL of the specified interests.
     * More restrictive than findUsersByAnyInterest().
     * 
     * @param interestNames List of interest names
     * @return List of user IDs who have all these interests
     * @throws SQLException if database operation fails
     */
    public List<String> findUsersByAllInterests(List<String> interestNames) throws SQLException {
        if (interestNames == null || interestNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Use HAVING COUNT to ensure user has ALL interests
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT userId FROM User_Interests WHERE interestName IN ("
        );
        
        for (int i = 0; i < interestNames.size(); i++) {
            sqlBuilder.append("?");
            if (i < interestNames.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(") GROUP BY userId HAVING COUNT(DISTINCT interestName) = ? ORDER BY userId ASC");
        
        String sql = sqlBuilder.toString();
        List<String> userIds = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Set interest name parameters
            for (int i = 0; i < interestNames.size(); i++) {
                ps.setString(i + 1, interestNames.get(i));
            }
            
            // Set count parameter
            ps.setInt(interestNames.size() + 1, interestNames.size());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getString("userId"));
                }
            }
            
            return userIds;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding users by all interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all unique interest names in the system.
     * Useful for displaying available interests to users.
     * 
     * @return List of all unique interest names
     * @throws SQLException if database operation fails
     */
    public List<String> findAllUniqueInterests() throws SQLException {
        String sql = "SELECT DISTINCT interestName FROM User_Interests ORDER BY interestName ASC";
        List<String> interests = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                interests.add(rs.getString("interestName"));
            }
            
            return interests;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding all unique interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the most popular interests (by user count).
     * Useful for trending interests or recommendations.
     * 
     * @param limit Maximum number of interests to return
     * @return List of interest names ordered by popularity
     * @throws SQLException if database operation fails
     */
    public List<String> findMostPopularInterests(int limit) throws SQLException {
        String sql = "SELECT interestName, COUNT(*) as userCount FROM User_Interests " +
                     "GROUP BY interestName ORDER BY userCount DESC, interestName ASC LIMIT ?";
        List<String> interests = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limit);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interests.add(rs.getString("interestName"));
                }
            }
            
            return interests;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding most popular interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all interests from the database.
     * 
     * @return List of all interests
     * @throws SQLException if database operation fails
     */
    public List<Interest> findAll() throws SQLException {
        String sql = "SELECT * FROM User_Interests ORDER BY userId ASC, interestName ASC";
        List<Interest> interests = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                interests.add(mapResultSetToInterest(rs));
            }
            
            return interests;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding all interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a user has a specific interest.
     * 
     * @param userId The user ID
     * @param interestName The interest name
     * @return true if user has this interest, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean existsForUser(String userId, String interestName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User_Interests WHERE userId = ? AND interestName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            ps.setString(2, interestName);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking interest existence: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the count of interests for a specific user.
     * 
     * @param userId The user ID
     * @return Number of interests for that user
     * @throws SQLException if database operation fails
     */
    public int countByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User_Interests WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error counting interests by user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the count of users who have a specific interest.
     * 
     * @param interestName The interest name
     * @return Number of users with this interest
     * @throws SQLException if database operation fails
     */
    public int countByInterest(String interestName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User_Interests WHERE interestName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, interestName);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error counting users by interest: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets total count of all interest entries (admin statistics).
     * 
     * @return Total interest entry count
     * @throws SQLException if database operation fails
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM User_Interests";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error counting all interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets total count of unique interests in the system.
     * 
     * @return Number of unique interest names
     * @throws SQLException if database operation fails
     */
    public int countUniqueInterests() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT interestName) FROM User_Interests";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error counting unique interests: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Replaces all interests for a user with a new set.
     * Deletes existing interests and adds new ones in a transaction.
     * 
     * @param userId The user ID
     * @param newInterests List of new interest names
     * @return true if replacement successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean replaceInterestsForUser(String userId, List<String> newInterests) 
            throws SQLException {
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete existing interests
            String deleteSql = "DELETE FROM User_Interests WHERE userId = ?";
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setString(1, userId);
                deletePs.executeUpdate();
            }
            
            // Add new interests
            if (newInterests != null && !newInterests.isEmpty()) {
                String insertSql = "INSERT INTO User_Interests (userId, interestName) VALUES (?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    for (String interestName : newInterests) {
                        insertPs.setString(1, userId);
                        insertPs.setString(2, interestName);
                        insertPs.executeUpdate();
                    }
                }
            }
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Error replacing interests for user: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    System.err.println("❌ Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Maps a ResultSet row to an Interest object.
     * 
     * @param rs ResultSet positioned at a valid row
     * @return Interest object
     * @throws SQLException if column access fails
     */
    private Interest mapResultSetToInterest(ResultSet rs) throws SQLException {
        String userId = rs.getString("userId");
        String interestName = rs.getString("interestName");
        
        return new Interest(userId, interestName);
    }
}