package com.connect.model;

/**
 * Interest - represents user interests for event recommendations.
 * Maps to User_Interests table in database.
 * 
 * Composite Primary Key: (userId, interestName)
 * No separate interest categories or IDs in this simplified design.
 * 
 * @author Muhammad Fatik Bin Imran (23i-0655)
 * @author Obaidullah Shoaib (23i-0609)
 */
public class Interest {
    private String userId;
    private String interestName;

    // Constructor
    public Interest(String userId, String interestName) {
        this.userId = userId;
        this.interestName = interestName;
    }

    // Business logic methods
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty()
                && interestName != null && !interestName.trim().isEmpty();
    }

    // Getters and Setters
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public String getInterestName() { 
        return interestName; 
    }
    
    public void setInterestName(String interestName) { 
        this.interestName = interestName; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interest interest = (Interest) o;
        return userId.equals(interest.userId) && interestName.equals(interest.interestName);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() + interestName.hashCode();
    }
    
    @Override
    public String toString() {
        return "Interest{" +
                "userId='" + userId + '\'' +
                ", interestName='" + interestName + '\'' +
                '}';
    }
}