package com.connect.model;

import java.time.LocalDateTime;
import com.connect.enums.UserType;
import com.connect.enums.AccountStatus;

/**
 * RegularUser - represents standard platform users who can both participate and organize events.
 * 
 * Design Pattern: Concrete implementation of Template Method
 * OOP Principle: Inheritance, Polymorphism
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class RegularUser extends User {
    
    public RegularUser(String userId, String name, String email, String phone, String password) {
        super(userId, name, email, phone, password, UserType.REGULAR);
    }
    
    public RegularUser(String userId, String name, String email, String phone, String password,
                      String profilePicture, String bio, AccountStatus accountStatus,
                      LocalDateTime createdAt, LocalDateTime lastLogin) {
        super(userId, name, email, phone, password, UserType.REGULAR, 
              profilePicture, bio, accountStatus, createdAt, lastLogin);
    }

    @Override
    public String getUserRole() {
        return "Regular User";
    }

    @Override
    public boolean canModerateEvents() {
        return false; // Regular users cannot moderate events
    }
    
    @Override
    public boolean canModerateReviews() {
        return false; // Regular users cannot moderate reviews
    }
    
    @Override
    public boolean canModerateUsers() {
        return false; // Regular users cannot moderate other users
    }

    // Regular user specific methods
    public boolean canCreateEvent() {
        return isActive(); // Active regular users can create events
    }
    
    public boolean canRegisterForEvent() {
        return isActive(); // Active regular users can register for events
    }
    
    public boolean canSubmitReview() {
        return isActive(); // Active regular users can submit reviews
    }
}