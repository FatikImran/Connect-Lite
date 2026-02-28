package com.connect.repository;

import com.connect.model.User;
import com.connect.model.RegularUser;
import com.connect.enums.UserType;
import com.connect.enums.AccountStatus;
import com.connect.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository class for User data access.
 * Handles all database operations for Users table.
 * 
 * Design Pattern: Repository Pattern
 * GRASP Principle: Information Expert (knows how to access user data)
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class UserRepository {

    /**
     * Creates a new user in the database.
     * 
     * @param user User object to create (RegularUser or Admin)
     * @return true if creation successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (userId, name, email, phone, password, userType, " +
                     "profilePicture, bio, accountStatus, createdAt, lastLogin) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getUserType().name());
            ps.setString(7, user.getProfilePicture());
            ps.setString(8, user.getBio());
            ps.setString(9, user.getAccountStatus().name());
            ps.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
            ps.setTimestamp(11, user.getLastLogin() != null ? 
                            Timestamp.valueOf(user.getLastLogin()) : null);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds a user by their unique ID.
     * Returns appropriate subclass (RegularUser or Admin) based on userType.
     * 
     * @param userId The user ID to search for
     * @return User object (RegularUser or Admin) or null if not found
     * @throws SQLException if database operation fails
     */
    public User findById(String userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding user by ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Finds a user by their email address.
     * Useful for login and duplicate email checks.
     * 
     * @param email The email to search for
     * @return User object or null if not found
     * @throws SQLException if database operation fails
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding user by email: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return List of all users
     * @throws SQLException if database operation fails
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM Users ORDER BY createdAt DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
            return users;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding all users: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves users by their type (REGULAR or ADMIN).
     * 
     * @param userType The type of users to retrieve
     * @return List of users of specified type
     * @throws SQLException if database operation fails
     */
    public List<User> findByUserType(UserType userType) throws SQLException {
        String sql = "SELECT * FROM Users WHERE userType = ? ORDER BY createdAt DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userType.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
            return users;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding users by type: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves users by their account status.
     * Useful for admin dashboards to see blocked/suspended users.
     * 
     * @param status The account status to filter by
     * @return List of users with specified status
     * @throws SQLException if database operation fails
     */
    public List<User> findByAccountStatus(AccountStatus status) throws SQLException {
        String sql = "SELECT * FROM Users WHERE accountStatus = ? ORDER BY createdAt DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
            return users;
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding users by status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates an existing user's information.
     * 
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE Users SET name = ?, email = ?, phone = ?, password = ?, " +
                     "profilePicture = ?, bio = ?, accountStatus = ?, lastLogin = ? " +
                     "WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getProfilePicture());
            ps.setString(6, user.getBio());
            ps.setString(7, user.getAccountStatus().name());
            ps.setTimestamp(8, user.getLastLogin() != null ? 
                            Timestamp.valueOf(user.getLastLogin()) : null);
            ps.setString(9, user.getUserId());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates only the account status of a user.
     * Used by admins to block/unblock/suspend users.
     * 
     * @param userId The user ID to update
     * @param status The new account status
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateAccountStatus(String userId, AccountStatus status) throws SQLException {
        String sql = "UPDATE Users SET accountStatus = ? WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.name());
            ps.setString(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating account status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates the last login timestamp for a user.
     * Called when user successfully logs in.
     * 
     * @param userId The user ID to update
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateLastLogin(String userId) throws SQLException {
        String sql = "UPDATE Users SET lastLogin = ? WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating last login: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a user from the database.
     * CASCADE in database will handle related records.
     * 
     * @param userId The user ID to delete
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE userId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if an email already exists in the database.
     * Useful for registration validation.
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking email existence: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the total count of users by type.
     * Useful for admin statistics.
     * 
     * @param userType The type to count
     * @return Number of users of that type
     * @throws SQLException if database operation fails
     */
    public int countByUserType(UserType userType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE userType = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userType.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error counting users by type: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Maps a ResultSet row to a User object (RegularUser or Admin).
     * Factory method that creates appropriate subclass based on userType.
     * 
     * @param rs ResultSet positioned at a valid row
     * @return User object (RegularUser or Admin)
     * @throws SQLException if column access fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String userId = rs.getString("userId");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String password = rs.getString("password");
        String userTypeStr = rs.getString("userType");
        String profilePicture = rs.getString("profilePicture");
        String bio = rs.getString("bio");
        String accountStatusStr = rs.getString("accountStatus");
        
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;
        
        Timestamp lastLoginTs = rs.getTimestamp("lastLogin");
        LocalDateTime lastLogin = lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null;
        
        UserType userType = UserType.valueOf(userTypeStr);
        AccountStatus accountStatus = AccountStatus.valueOf(accountStatusStr);

        // Create RegularUser for all user types
        return new RegularUser(userId, name, email, phone, password,
                             profilePicture, bio, accountStatus, createdAt, lastLogin);
    }
    
    
    /**
     * Searches users by name (partial match, case-insensitive).
     * Useful for admin user search functionality.
     * 
     * @param query The search query
     * @return List of users matching the query
     * @throws SQLException if database operation fails
     */
    public List<User> searchByName(String query) throws SQLException {
        String sql = "SELECT * FROM Users WHERE name LIKE ? ORDER BY name ASC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + query + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
            return users;
            
        } catch (SQLException e) {
            System.err.println("❌ Error searching users by name: " + e.getMessage());
            throw e;
        }
    }
    

    /**
     * Gets admin dashboard statistics.
     * 
     * @return Map containing various statistics
     * @throws SQLException if database operation fails
     */
    public Map<String, Integer> getStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Total users
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Users");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalUsers", rs.getInt(1));
                }
            }
            
            // Regular users
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE userType = 'REGULAR'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("regularUsers", rs.getInt(1));
                }
            }
            
            // Admin users
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE userType = 'ADMIN'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("adminUsers", rs.getInt(1));
                }
            }
            
            // Active users
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE accountStatus = 'ACTIVE'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("activeUsers", rs.getInt(1));
                }
            }
            
            // Blocked users
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE accountStatus = 'BLOCKED'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("blockedUsers", rs.getInt(1));
                }
            }
            
            return stats;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting statistics: " + e.getMessage());
            throw e;
        }
    }
}