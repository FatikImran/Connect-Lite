package com.connect.enums;

/**
 * User type enumeration
 */
public enum UserType {
    REGULAR("Regular User"),
    ADMIN("Administrator");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}