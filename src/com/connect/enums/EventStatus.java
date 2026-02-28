package com.connect.enums;

/**
 * Event status enumeration
 */
public enum EventStatus {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    EventStatus(String displayName) {
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