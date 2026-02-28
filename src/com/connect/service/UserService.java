package com.connect.service;

import com.connect.model.User;
import com.connect.model.RegularUser;
import com.connect.repository.UserRepository;
import com.connect.repository.RegistrationRepository;
import com.connect.repository.EventRepository;
import com.connect.repository.InterestRepository;
import com.connect.model.Interest;
import java.util.Map;
import com.connect.util.IdGenerator;
import com.connect.util.PasswordUtil;
import com.connect.util.ValidationUtil;
import com.connect.enums.UserType;
import com.connect.enums.AccountStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UserService - handles all user-related business logic.
 * 
 * GRASP Patterns:
 * - Information Expert: Encapsulates user business rules
 * - Controller: Coordinates user operations
 * - High Cohesion: All user logic together
 * 
 * Design Pattern: Service Layer Pattern
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class UserService {
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final InterestRepository interestRepository;

    public UserService() {
        this.userRepository = new UserRepository();
        this.registrationRepository = new RegistrationRepository();
        this.eventRepository = new EventRepository();
        this.interestRepository = new InterestRepository();
    }

    /**
     * Register new user
     */
	  /**
     * Register new user
     */
    public User registerUser(String name, String email, String phone, String password, UserType userType) 
            throws SQLException, IllegalArgumentException {
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Generate user ID
        String userId = IdGenerator.generateUserId();
        
        // Hash password
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Create user based on type
        User user;
        if (userType == UserType.ADMIN) {
            // For demo purposes, new registrations can't be admins
            // Admins should be created by existing admins
            throw new IllegalArgumentException("Admin accounts cannot be created through registration");
        } else {
            user = new RegularUser(userId, name, email, phone, hashedPassword);
        }
        
        // Save user
        boolean saved = userRepository.createUser(user);
        if (!saved) {
            throw new SQLException("Failed to save user");
        }
        
        return user;
    }

    /**
     * Authenticate user
     */
    public User authenticateUser(String email, String password) 
            throws SQLException, IllegalArgumentException {
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not active");
        }
        
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Update last login
        user.updateLastLogin();
        userRepository.updateUser(user);
        
        return user;
    }

    /**
     * Get user statistics
     */
    public UserStats getUserStats(String userId) throws SQLException {
        int attendedEvents = registrationRepository.countAttendedByParticipant(userId);
        int totalRegistrations = 0; // You might want to add this method to registration repository
        
        return new UserStats(attendedEvents, totalRegistrations);
    }

    /**
     * User statistics DTO
     */
    public static class UserStats {
        public final int attendedEvents;
        public final int totalRegistrations;
        
        public UserStats(int attendedEvents, int totalRegistrations) {
            this.attendedEvents = attendedEvents;
            this.totalRegistrations = totalRegistrations;
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Other existing methods remain the same...
    
    public boolean updateUserProfile(String userId, String name, String email, String phone, String bio) 
            throws SQLException {
        User user = userRepository.findById(userId);
        if (user == null) return false;
        
        boolean updated = user.updateProfile(name, email, phone);
        if (updated && bio != null) {
            user.updateBio(bio);
        }
        
        return updated ? userRepository.updateUser(user) : false;
    }
    
    public boolean updatePassword(String userId, String currentPassword, String newPassword) 
            throws SQLException {
        User user = userRepository.findById(userId);
        if (user == null || !PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        return userRepository.updateUser(user);
    }
	
    public int getEventsAttendedCount(String userId) throws SQLException {
        return registrationRepository.countAttendedByParticipant(userId);
    }

    /**
     * Get number of events organized by the user
     */
    public int getEventsOrganizedCount(String userId) throws SQLException {
        return eventRepository.countByOrganizerId(userId);
    }

    /**
     * UC2: Sign Up - Create new user account
     * 
     * @param name User's full name
     * @param email User's email
     * @param phone User's phone number
     * @param password Plain text password
     * @param userType REGULAR or ADMIN
     * @return Created user object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public User signUp(String name, String email, String phone, String password, UserType userType) {
        try {
            System.out.println("🔍 [UserService] signUp() called");
            System.out.println("   Parameters received:");
            System.out.println("   - name: " + name);
            System.out.println("   - email: " + email);
            System.out.println("   - phone: " + phone);
            System.out.println("   - userType: " + userType);
            
            // 1. Check if email already exists
            System.out.println("🔍 [Step 1] Checking if email exists...");
            boolean exists = userRepository.emailExists(email);
            System.out.println("   Email exists: " + exists);
            
            if (exists) {
                System.err.println("❌ Email already exists: " + email);
                return null;
            }
            
            // 2. Generate unique user ID
            System.out.println("🔍 [Step 2] Generating user ID...");
            String userId = IdGenerator.generateUserId();
            System.out.println("   Generated User ID: " + userId);
            
            // 3. Hash the password
            System.out.println("🔍 [Step 3] Hashing password...");
            String hashedPassword = PasswordUtil.hashPassword(password);
            System.out.println("   Password hashed successfully (length: " + hashedPassword.length() + ")");
            
            // 4. Create appropriate User object
            System.out.println("🔍 [Step 4] Creating User object...");
            User user;
            if (userType == UserType.ADMIN) {
                System.out.println("   Creating Admin user - Admin accounts cannot be created through signup");
                return null;
            } else {
                System.out.println("   Creating RegularUser");
                user = new RegularUser(
                    userId, name, email, phone, hashedPassword,
                    null, // profilePicture
                    null, // bio
                    AccountStatus.ACTIVE,
                    LocalDateTime.now(),
                    null // lastLogin
                );
            }
            System.out.println("   User object created successfully");
            
            // 5. Save to database
            System.out.println("🔍 [Step 5] Saving to database...");
            boolean created = userRepository.createUser(user);
            System.out.println("   Database insert result: " + created);
            
            if (created) {
                System.out.println("✅ [UserService] User created successfully!");
                System.out.println("   User ID: " + user.getUserId());
                System.out.println("   User Name: " + user.getName());
                System.out.println("   User Email: " + user.getEmail());
                return user;
            } else {
                System.err.println("❌ [UserService] Database insert failed - createUser returned false");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ [UserService] SQLException during signup:");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ [UserService] Unexpected exception during signup:");
            System.err.println("   Exception Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * UC1: Login - Authenticate user
     * 
     * @param email User's email
     * @param password Plain text password
     * @return Authenticated user object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if credentials invalid
     */
    public User login(String email, String password) throws Exception {
        System.out.println("🔍 [UserService] login() called for: " + email);
        
        // Find user by email
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            System.err.println("❌ User not found with email: " + email);
            throw new Exception("Invalid email or password");
        }
        
        // Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            System.err.println("❌ Account is not active: " + user.getAccountStatus());
            throw new Exception("Account is " + user.getAccountStatus().toString().toLowerCase());
        }
        
        // Verify password
        boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getPassword());
        
        if (!passwordMatch) {
            System.err.println("❌ Password verification failed");
            throw new Exception("Invalid email or password");
        }
        
        // Update last login
        userRepository.updateLastLogin(user.getUserId());
        
        System.out.println("✅ Login successful for: " + user.getName());
        return user;
    }

    /**
     * UC7: Update Profile - Update user information
     * 
     * @param userId ID of user to update
     * @param name New name
     * @param email New email
     * @param phone New phone
     * @return true if update successful
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public boolean updateProfile(String userId, String name, String email, String phone) 
            throws SQLException, IllegalArgumentException {
        
        // 1. Validate inputs
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        // 2. Get existing user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // 3. Check if email is taken by another user
        if (!user.getEmail().equals(email) && userRepository.emailExists(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        // 4. Update user object
        boolean updated = user.updateProfile(name, email, phone);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update profile");
        }
        
        // 5. Save to database
        return userRepository.updateUser(user);
    }

    /**
     * UC14: Moderate Users - Block/Unblock/Delete user (Admin only)
     * 
     * @param adminId ID of admin performing action
     * @param userId ID of user to moderate
     * @param action "BLOCK", "UNBLOCK", or "DELETE"
     * @param reason Reason for moderation
     * @return true if action successful
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public boolean moderateUser(String adminId, String userId, String action, String reason) 
            throws SQLException, IllegalArgumentException {
        
        // 1. Verify admin permissions
        User admin = userRepository.findById(adminId);
        if (admin == null || !admin.canModerateEvents()) {
            throw new IllegalArgumentException("Unauthorized: Admin access required");
        }
        
        // 2. Get target user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // 3. Prevent self-moderation
        if (adminId.equals(userId)) {
            throw new IllegalArgumentException("Cannot moderate your own account");
        }
        
        // 4. Prevent moderating other admins
        if (user.getUserType() == UserType.ADMIN) {
            throw new IllegalArgumentException("Cannot moderate admin accounts");
        }
        
        // 5. Perform action
        switch (action.toUpperCase()) {
            case "BLOCK":
                user.setAccountStatus(AccountStatus.BLOCKED);
                return userRepository.updateUser(user);
                
            case "UNBLOCK":
                user.setAccountStatus(AccountStatus.ACTIVE);
                return userRepository.updateUser(user);
                
            case "DELETE":
                // Check for active registrations
                if (hasActiveRegistrations(userId)) {
                    throw new IllegalArgumentException(
                        "User has active registrations. Cancel them first or proceed with cascade delete.");
                }
                return userRepository.deleteUser(userId);
                
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(String userId) throws SQLException {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) throws SQLException {
        return userRepository.findByEmail(email);
    }

    /**
     * Search users by name (for admin)
     */
    public List<User> searchUsers(String query, String adminId) 
            throws SQLException, IllegalArgumentException {
        
        // Verify admin access
        User admin = userRepository.findById(adminId);
        if (admin == null || !admin.canModerateEvents()) {
            throw new IllegalArgumentException("Unauthorized: Admin access required");
        }
        
        return userRepository.searchByName(query);
    }

    /**
     * Get all users (for admin)
     */
    public List<User> getAllUsers(String adminId) 
            throws SQLException, IllegalArgumentException {
        
        // Verify admin access
        User admin = userRepository.findById(adminId);
        if (admin == null || !admin.canModerateEvents()) {
            throw new IllegalArgumentException("Unauthorized: Admin access required");
        }
        
        return userRepository.findAll();
    }

    /**
     * Get user interests
     */
    public List<Interest> getUserInterests(String userId) throws SQLException {
        return interestRepository.findByUserId(userId);
    }

    /**
     * Add interest to user
     */
    public boolean addInterest(String userId, String interestName) 
            throws SQLException, IllegalArgumentException {
        
        // Validate
        if (interestName == null || interestName.trim().isEmpty()) {
            throw new IllegalArgumentException("Interest name is required");
        }
        
        // Check if user exists
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Create and save interest
        Interest interest = new Interest(userId, interestName.trim());
        return interestRepository.addInterest(interest);
    }

    /**
     * Remove interest from user
     */
    public boolean removeInterest(String userId, String interestName) throws SQLException {
        return interestRepository.removeInterest(userId, interestName);
    }

    /**
     * Get admin dashboard statistics
     */
    public Map<String, Integer> getAdminStatistics(String adminId) 
            throws SQLException, IllegalArgumentException {
        
        // Verify admin access
        User admin = userRepository.findById(adminId);
        if (admin == null || !admin.canModerateEvents()) {
            throw new IllegalArgumentException("Unauthorized: Admin access required");
        }
        
        return userRepository.getStatistics();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate sign-up data
     */
    private void validateSignUpData(String name, String email, String phone, String password) 
            throws IllegalArgumentException {
        
        if (!ValidationUtil.isValidName(name)) {
            throw new IllegalArgumentException("Invalid name format");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters with letters and numbers");
        }
    }

    /**
     * Check if user has active event registrations
     */
    private boolean hasActiveRegistrations(String userId) throws SQLException {
        // This will be called from RegistrationRepository
        // For now, return false to allow compilation
        // TODO: Implement after RegistrationRepository is ready
        return false;
    }
}
