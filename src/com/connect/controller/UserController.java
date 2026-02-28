package com.connect.controller;

import com.connect.model.User;
import com.connect.model.Interest;
import com.connect.service.UserService;
import com.connect.util.SessionManager;

import java.util.List;
import java.util.Map;

/**
 * UserController - handles user profile and management operations.
 * 
 * GRASP Patterns:
 * - Controller: Coordinates user operations
 * - Low Coupling: Delegates to UserService
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    /**
     * UC7: Handle Update Profile
     * 
     * @param name New name
     * @param email New email
     * @param phone New phone
     * @return true if successful
     */
    public boolean handleUpdateProfile(String name, String email, String phone) {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            
            boolean updated = userService.updateProfile(userId, name, email, phone);
            
            if (updated) {
                System.out.println("Profile updated successfully");
                // Refresh session with updated user
                User updatedUser = userService.getUserById(userId);
                SessionManager.login(updatedUser);
            }
            return updated;
            
        } catch (Exception e) {
            System.err.println("Update profile failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get current user profile
     */
    public User handleGetMyProfile() {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            return userService.getUserById(userId);
        } catch (Exception e) {
            System.err.println("Get profile failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user by ID
     */
    public User handleGetUserProfile(String userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            System.err.println("Get user profile failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * UC14: Handle Moderate User - Block user (Admin only)
     */
    public boolean handleBlockUser(String userId, String reason) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            
            boolean blocked = userService.moderateUser(adminId, userId, "BLOCK", reason);
            
            if (blocked) {
                System.out.println("User blocked successfully");
            }
            return blocked;
            
        } catch (Exception e) {
            System.err.println("Block user failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * UC14: Handle Moderate User - Unblock user (Admin only)
     */
    public boolean handleUnblockUser(String userId) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            
            boolean unblocked = userService.moderateUser(adminId, userId, "UNBLOCK", "");
            
            if (unblocked) {
                System.out.println("User unblocked successfully");
            }
            return unblocked;
            
        } catch (Exception e) {
            System.err.println("Unblock user failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * UC14: Handle Moderate User - Delete user (Admin only)
     */
    public boolean handleDeleteUser(String userId, String reason) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            
            boolean deleted = userService.moderateUser(adminId, userId, "DELETE", reason);
            
            if (deleted) {
                System.out.println("User deleted successfully");
            }
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Delete user failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search users (Admin only)
     */
    public List<User> handleSearchUsers(String query) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            return userService.searchUsers(query, adminId);
        } catch (Exception e) {
            System.err.println("Search users failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get all users (Admin only)
     */
    public List<User> handleGetAllUsers() {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            return userService.getAllUsers(adminId);
        } catch (Exception e) {
            System.err.println("Get all users failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get user interests
     */
    public List<Interest> handleGetMyInterests() {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            return userService.getUserInterests(userId);
        } catch (Exception e) {
            System.err.println("Get interests failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get number of events the current user attended
     */
    public int handleGetEventsAttendedCount() {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            return userService.getEventsAttendedCount(userId);
        } catch (Exception e) {
            System.err.println("Get events attended count failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get number of events the current user organized
     */
    public int handleGetEventsOrganizedCount() {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            return userService.getEventsOrganizedCount(userId);
        } catch (Exception e) {
            System.err.println("Get events organized count failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Add interest
     */
    public boolean handleAddInterest(String interestName) {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            
            boolean added = userService.addInterest(userId, interestName);
            
            if (added) {
                System.out.println("Interest added successfully");
            }
            return added;
            
        } catch (Exception e) {
            System.err.println("Add interest failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove interest
     */
    public boolean handleRemoveInterest(String interestName) {
        try {
            String userId = SessionManager.getCurrentUser().getUserId();
            
            boolean removed = userService.removeInterest(userId, interestName);
            
            if (removed) {
                System.out.println("Interest removed successfully");
            }
            return removed;
            
        } catch (Exception e) {
            System.err.println("Remove interest failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get admin dashboard statistics
     */
    public Map<String, Integer> handleGetAdminStatistics() {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            return userService.getAdminStatistics(adminId);
        } catch (Exception e) {
            System.err.println("Get admin statistics failed: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Check if current user is admin
     */
    public boolean isCurrentUserAdmin() {
        return SessionManager.isAdmin();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    

    /**
     * Get current user name
     */
    public String getCurrentUserName() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getName() : null;
    }
    
    public boolean handleBlockUser(String userId) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            return userService.moderateUser(adminId, userId, "BLOCK", "Blocked by admin");
        } catch (Exception e) {
            System.err.println("Block user failed: " + e.getMessage());
            return false;
        }
    }
    
    public boolean handleDeleteUser(String userId) {
        try {
            String adminId = SessionManager.getCurrentUser().getUserId();
            return userService.moderateUser(adminId, userId, "DELETE", "Deleted by admin");
        } catch (Exception e) {
            System.err.println("Delete user failed: " + e.getMessage());
            return false;
        }
    }
    
}