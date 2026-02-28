package com.connect.controller;

import com.connect.model.User;
import com.connect.service.UserService;
import com.connect.util.SessionManager;
import com.connect.enums.UserType;

/**
 * AuthController - handles authentication operations.
 * 
 * GRASP Patterns:
 * - Controller: Delegates user authentication to UserService
 * - Low Coupling: Doesn't know about UI or database details
 * - High Cohesion: Only handles authentication logic
 * 
 * Design Pattern: Facade (simplifies authentication for UI layer)
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class AuthController {
    private final UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

    /**
     * UC1: Handle Login
     * 
     * @param email User's email
     * @param password User's password
     * @return Authenticated user or null if failed
     */
    public User handleLogin(String email, String password) {
        try {
            // 1. Authenticate user via service
            User user = userService.login(email, password);
            
            // 2. Store in session
            SessionManager.login(user);
            
            System.out.println("Login successful: " + user.getName());
            return user;
            
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * UC2: Handle Sign Up
     * 
     * @param name User's full name
     * @param email User's email
     * @param phone User's phone
     * @param password User's password
     * @param isAdmin Whether to create admin account
     * @return Created user or null if failed
     */
    public User handleSignUp(String name, String email, String phone, 
                            String password, boolean isAdmin) {
        try {
            // Determine user type
            UserType userType = isAdmin ? UserType.ADMIN : UserType.REGULAR;
            
            // Create user via service
            User user = userService.signUp(name, email, phone, password, userType);
            
            // Auto-login after signup
            SessionManager.login(user);
            
            System.out.println("Sign up successful: " + user.getName());
            return user;
            
        } catch (Exception e) {
            System.err.println("Sign up failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handle Logout
     */
    public void handleLogout() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            System.out.println("Logout: " + currentUser.getName());
        }
        SessionManager.logout();
    }

    /**
     * Get currently logged-in user
     */
    public User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return SessionManager.isLoggedIn();
    }

    /**
     * Check if current user is admin
     */
    public boolean isCurrentUserAdmin() {
        return SessionManager.isAdmin();
    }

    /**
     * Validate email format (helper for UI)
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validate password strength (helper for UI)
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Get error message from exception
     */
    public String getErrorMessage(Exception e) {
        return e.getMessage();
    }
}