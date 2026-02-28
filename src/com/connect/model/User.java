package com.connect.model;

import java.time.LocalDateTime;
import com.connect.enums.UserType;
import com.connect.enums.AccountStatus;

/**
 * Abstract base class for all user types.
 * Maps directly to the Users table in database.
 * 
 * Design Pattern: Template Method (defines common structure)
 * OOP Principle: Inheritance, Abstraction, Encapsulation
 * 
 * @author Obaidullah Shoaib (23i-0609)
 * @author Muhammad Fatik Bin Imran (23i-0655)
 */
public abstract class User {
    // Primary fields (match DB schema exactly)
    protected String userId;
    protected String name;
    protected String email;
    protected String phone;
    protected String password;
    protected UserType userType;
    
    // Profile fields (ALL users have these in DB)
    protected String profilePicture;
    protected String bio;
    
    // Status and metadata
    protected AccountStatus accountStatus;
    protected LocalDateTime createdAt;
    protected LocalDateTime lastLogin;

    // Constructor for new users
    public User(String userId, String name, String email, String phone, 
                String password, UserType userType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.userType = userType;
        this.accountStatus = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor (for loading from database)
    public User(String userId, String name, String email, String phone, String password,
                UserType userType, String profilePicture, String bio, 
                AccountStatus accountStatus, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.userType = userType;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Abstract method - must be implemented by subclasses
    public abstract String getUserRole();
    
    // Abstract method - for type-specific permissions
    public abstract boolean canModerateEvents();
    
    // Abstract method - for review moderation permissions
    public abstract boolean canModerateReviews();
    
    // Abstract method - for user management permissions
    public abstract boolean canModerateUsers();

    // Business logic methods (Information Expert pattern)
    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }
    
    public boolean isBlocked() {
        return this.accountStatus == AccountStatus.BLOCKED;
    }
    
    public boolean isSuspended() {
        return this.accountStatus == AccountStatus.SUSPENDED;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean updateProfile(String name, String email, String phone) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (!isValidEmail(email)) {
            return false;
        }
        this.name = name;
        this.email = email;
        this.phone = phone;
        return true;
    }
    
    public void updateBio(String bio) {
        this.bio = bio;
    }
    
    public void updateProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Getters and Setters
    public String getUserId() { 
        return userId; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }
    
    public UserType getUserType() { 
        return userType; 
    }
    
    protected void setUserType(UserType userType) { 
        this.userType = userType; 
    }
    
    public String getProfilePicture() { 
        return profilePicture; 
    }
    
    public void setProfilePicture(String profilePicture) { 
        this.profilePicture = profilePicture; 
    }
    
    public String getBio() { 
        return bio; 
    }
    
    public void setBio(String bio) { 
        this.bio = bio; 
    }
    
    public AccountStatus getAccountStatus() { 
        return accountStatus; 
    }
    
    public void setAccountStatus(AccountStatus accountStatus) { 
        this.accountStatus = accountStatus; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getLastLogin() { 
        return lastLogin; 
    }
    
    public void setLastLogin(LocalDateTime lastLogin) { 
        this.lastLogin = lastLogin; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                ", accountStatus=" + accountStatus +
                '}';
    }
}