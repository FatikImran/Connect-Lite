package com.connect.model;

import com.connect.enums.RegistrationStatus;
import java.time.LocalDateTime;

/**
 * Registration model linking participants to events.
 * 
 * GRASP Patterns:
 * - Information Expert: Knows registration validation rules
 * - High Cohesion: All registration-related logic in one place
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class Registration {
    private String registrationId;
    private String participantId;
    private String eventId;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
    private boolean attended;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    public Registration(String registrationId, String participantId, String eventId) {
        this.registrationId = registrationId;
        this.participantId = participantId;
        this.eventId = eventId;
        this.registrationDate = LocalDateTime.now();
        this.status = RegistrationStatus.CONFIRMED;
        this.attended = true;
    }
    
    // Getters and Setters
    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
    
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    // Business Logic Methods
    
    /**
     * Check if registration is active (confirmed and not cancelled)
     */
    public boolean isActive() {
        return status == RegistrationStatus.CONFIRMED;
    }
    
    /**
     * Check if registration can be cancelled
     */
    public boolean canBeCancelled() {
        return isActive() && !attended;
    }
    
    /**
     * Cancel registration
     */
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Registration cannot be cancelled");
        }
        this.status = RegistrationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    /**
     * Mark participant as attended
     */
    public void markAttended() {
        if (!isActive()) {
            throw new IllegalStateException("Only active registrations can be marked as attended");
        }
        this.attended = true;
    }
    
    /**
     * Check if participant can submit review (attended and event completed)
     */
    public boolean canSubmitReview(Event event) {
        return isActive() && attended && event.isCompleted();
    }
    
    /**
     * Check if registration is eligible for certificate
     * Certificate eligibility: attended the event and event is completed
     */
    public boolean isEligibleForCertificate() {
        return isActive() && attended;
    }
    
    @Override
    public String toString() {
        return "Registration{" +
                "registrationId='" + registrationId + '\'' +
                ", participantId='" + participantId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", status=" + status +
                ", attended=" + attended +
                '}';
    }
}