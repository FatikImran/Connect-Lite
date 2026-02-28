package com.connect.enums;

/**
 * Account status enumeration
 */
public enum AccountStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    SUSPENDED("Suspended"),
    DELETED("Deleted");

    private final String displayName;

    AccountStatus(String displayName) {
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