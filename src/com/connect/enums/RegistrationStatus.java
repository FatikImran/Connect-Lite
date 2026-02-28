package com.connect.enums;

/**
 * Registration status enumeration
 */
public enum RegistrationStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    WAITLISTED("Waitlisted"),
    ATTENDED("Attended");

    private final String displayName;

    RegistrationStatus(String displayName) {
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