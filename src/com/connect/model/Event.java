package com.connect.model;

import com.connect.enums.EventStatus;
import java.time.LocalDateTime;

/**
 * Event model representing an event in the Connect platform.
 * 
 * GRASP Patterns:
 * - Information Expert: Knows event-related business rules
 * - High Cohesion: All event-related logic in one place
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class Event {
    private String eventId;
    private String organizerId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String venue;
    private String category;
    private int capacity;
    private int currentRegistrations;
    private LocalDateTime registrationDeadline;
    private EventStatus eventStatus;
    private LocalDateTime createdAt;
    private String approvedBy;
    private String rejectionReason;
    
    public Event(String eventId, String organizerId, String title, String description,
                LocalDateTime startDateTime, LocalDateTime endDateTime, String venue,
                String category, int capacity, LocalDateTime registrationDeadline) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.venue = venue;
        this.category = category;
        this.capacity = capacity;
        this.currentRegistrations = 0;
        this.registrationDeadline = registrationDeadline;
        this.eventStatus = EventStatus.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public int getCurrentRegistrations() { return currentRegistrations; }
    public void setCurrentRegistrations(int currentRegistrations) { 
        this.currentRegistrations = currentRegistrations; 
    }
    
    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime registrationDeadline) { 
        this.registrationDeadline = registrationDeadline; 
    }
    
    public EventStatus getEventStatus() { return eventStatus; }
    public void setEventStatus(EventStatus eventStatus) { this.eventStatus = eventStatus; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    // Business Logic Methods
    
    /**
     * Check if event has capacity for more registrations
     */
    public boolean hasCapacity() {
        return currentRegistrations < capacity;
    }
    
    /**
     * Check if registration is still open
     */
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(registrationDeadline) && 
               hasCapacity() && 
               eventStatus == EventStatus.APPROVED;
    }
    
    /**
     * Check if event can be updated
     */
    public boolean canBeUpdated() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startDateTime) && 
               eventStatus != EventStatus.COMPLETED && 
               eventStatus != EventStatus.CANCELLED;
    }
    
    /**
     * Check if event can be cancelled
     */
    public boolean canBeCancelled() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startDateTime) && 
               eventStatus != EventStatus.COMPLETED && 
               eventStatus != EventStatus.CANCELLED;
    }
    
    /**
     * Check if event is completed
     */
    public boolean isCompleted() {
        return eventStatus == EventStatus.COMPLETED || 
               (endDateTime != null && LocalDateTime.now().isAfter(endDateTime));
    }
    
    /**
     * Check if event is ongoing
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startDateTime != null && endDateTime != null &&
               now.isAfter(startDateTime) && now.isBefore(endDateTime) &&
               eventStatus == EventStatus.APPROVED;
    }
    
    /**
     * Check if user is the organizer of this event
     */
    public boolean isOrganizer(String userId) {
        return organizerId.equals(userId);
    }
    
    /**
     * Approve event
     */
    public void approve(String adminId) {
        this.eventStatus = EventStatus.APPROVED;
        this.approvedBy = adminId;
        this.rejectionReason = null;
    }
    
    /**
     * Reject event
     */
    public void reject(String adminId, String reason) {
        this.eventStatus = EventStatus.REJECTED;
        this.approvedBy = adminId;
        this.rejectionReason = reason;
    }
    
    /**
     * Cancel event
     */
    public void cancel(String reason) {
        this.eventStatus = EventStatus.CANCELLED;
        this.rejectionReason = reason;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", title='" + title + '\'' +
                ", status=" + eventStatus +
                ", startDateTime=" + startDateTime +
                ", capacity=" + currentRegistrations + "/" + capacity +
                '}';
    }
}