package com.connect.util;

import com.connect.model.User;
import com.connect.enums.UserType;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;

/**
 * SessionManager - manages user session state across the application.
 * 
 * Design Pattern: Singleton (static class holding single session)
 * GRASP Principle: Information Expert (knows about current session)
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public class SessionManager {
    private static User currentUser;
    private static LocalDateTime loginTime;
    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);
    
    // Preference keys
    private static final String REMEMBERED_EMAIL = "remembered_email";
    private static final String USER_ID = "user_id";
    private static final String USER_TYPE = "user_type";
    private static final String USER_NAME = "user_name";
    
    /**
     * Log in a user and establish session.
     * This is the primary method for starting a user session.
     * 
     * @param user The authenticated user to log in
     */
    public static void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot login null user");
        }
        
        currentUser = user;
        loginTime = LocalDateTime.now();
        
        // Persist to preferences for session restoration
        prefs.put(USER_ID, user.getUserId());
        prefs.put(USER_TYPE, user.getUserType().name());
        prefs.put(USER_NAME, user.getName());
        
        System.out.println("✓ Session established for: " + user.getName() + 
                          " (" + user.getUserType() + ")");
    }
    
    /**
     * Set current user (alias for login, kept for backward compatibility).
     * Prefer using login() for clarity.
     * 
     * @param user The user to set as current
     */
    public static void setCurrentUser(User user) {
        login(user);
    }
    
    /**
     * Get the currently logged-in user.
     * 
     * @return Current user or null if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get the time when current user logged in.
     * 
     * @return Login timestamp or null if not logged in
     */
    public static LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Get current user's ID.
     * 
     * @return User ID or null if not logged in
     */
    public static String getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    /**
     * Get current user's name.
     * 
     * @return User name or null if not logged in
     */
    public static String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : null;
    }
    
    /**
     * Get current user's type.
     * 
     * @return UserType or null if not logged in
     */
    public static UserType getCurrentUserType() {
        return currentUser != null ? currentUser.getUserType() : null;
    }
    
    /**
     * Check if a user is currently logged in.
     * 
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user is an admin.
     * 
     * @return true if current user is admin, false otherwise
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getUserType() == UserType.ADMIN;
    }
    
    /**
     * Log out the current user and clear session.
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("✓ Session ended for: " + currentUser.getName());
        }
        
        currentUser = null;
        loginTime = null;
        
        // Clear stored session data
        prefs.remove(USER_ID);
        prefs.remove(USER_TYPE);
        prefs.remove(USER_NAME);
    }
    
    /**
     * Remember user's email for future logins (optional feature).
     * Alias for rememberUser() - kept for consistency.
     * 
     * @param email Email to remember
     */
    public static void rememberUser(String email) {
        if (email != null && !email.trim().isEmpty()) {
            prefs.put(REMEMBERED_EMAIL, email);
        }
    }
    
    /**
     * Get remembered email from previous sessions.
     * 
     * @return Remembered email or null if none
     */
    public static String getRememberedUser() {
        return prefs.get(REMEMBERED_EMAIL, null);
    }
    
    /**
     * Clear remembered email.
     */
    public static void clearRememberedUser() {
        prefs.remove(REMEMBERED_EMAIL);
    }
    
    /**
     * Remember user's email (new naming convention).
     * 
     * @param email Email to remember
     */
    public static void rememberEmail(String email) {
        rememberUser(email);
    }
    
    /**
     * Get remembered email (new naming convention).
     * 
     * @return Remembered email or null if none
     */
    public static String getRememberedEmail() {
        return getRememberedUser();
    }
    
    /**
     * Clear remembered email (new naming convention).
     */
    public static void clearRememberedEmail() {
        clearRememberedUser();
    }
    
    /**
     * Restore user session from stored preferences.
     * Useful for "Remember Me" functionality.
     * 
     * Note: This only restores basic user info. Full authentication
     * should be done via login for security.
     * 
     * @return true if session was restored, false otherwise
     */
    public static boolean restoreSession() {
        String userId = prefs.get(USER_ID, null);
        String userTypeStr = prefs.get(USER_TYPE, null);
        String userName = prefs.get(USER_NAME, null);
        
        if (userId != null && userTypeStr != null && userName != null) {
            try {
                // Note: This creates a minimal user object
                // Real implementation should fetch full user from database
                System.out.println("⚠ Session restoration attempted but requires full user fetch");
                return false;
            } catch (Exception e) {
                System.err.println("Failed to restore session: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Get stored user type from preferences.
     * 
     * @return Stored UserType or null if not found
     */
    public static UserType getStoredUserType() {
        String userTypeStr = prefs.get(USER_TYPE, null);
        if (userTypeStr != null) {
            try {
                return UserType.valueOf(userTypeStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid user type in preferences: " + userTypeStr);
                return null;
            }
        }
        return null;
    }
    
    /**
     * Validate that a user session exists and is active.
     * 
     * @throws IllegalStateException if no user is logged in
     */
    public static void requireLogin() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("User must be logged in to perform this action");
        }
    }
    
    /**
     * Validate that current user is an admin.
     * 
     * @throws IllegalStateException if user is not admin
     */
    public static void requireAdmin() {
        requireLogin();
        if (!isAdmin()) {
            throw new IllegalStateException("Admin privileges required for this action");
        }
    }
    
    /**
     * Clear all session data and preferences.
     * Use with caution - this completely resets the session.
     */
    public static void clearAll() {
        logout();
        clearRememberedEmail();
        System.out.println("✓ All session data cleared");
    }

	public static void clearSession() {
		// TODO Auto-generated method stub
		
	}
}
