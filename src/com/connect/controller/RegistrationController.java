package com.connect.controller;

import com.connect.model.Registration;
import com.connect.service.RegistrationService;
import com.connect.util.SessionManager;

import java.util.List;

/**
 * RegistrationController - handles registration operations.
 * 
 * GRASP Patterns:
 * - Controller: Coordinates registration operations
 * - Low Coupling: Delegates to RegistrationService
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController() {
        this.registrationService = new RegistrationService();
    }

    /**
     * UC4: Handle Register for Event
     * 
     * @param eventId ID of event to register for
     * @return Created registration or null if failed
     */
    public Registration handleRegisterForEvent(String eventId) {
        try {
            if (!SessionManager.isLoggedIn()) {
                System.err.println("Register for event failed: user not logged in");
                return null;
            }

            String participantId = SessionManager.getCurrentUser().getUserId();

            Registration registration = registrationService.registerForEvent(participantId, eventId);
            
            System.out.println("Registration successful for event: " + eventId);
            return registration;
            
        } catch (Exception e) {
            System.err.println("Register for event failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * UC6: Handle Cancel Registration
     * 
     * @param registrationId ID of registration to cancel
     * @param reason Reason for cancellation
     * @return true if cancellation successful
     */
    public boolean handleCancelRegistration(String registrationId, String reason) {
        try {
            if (!SessionManager.isLoggedIn()) {
                System.err.println("Cancel registration failed: user not logged in");
                return false;
            }

            boolean cancelled = registrationService.cancelRegistration(registrationId, reason);
            
            if (cancelled) {
                System.out.println("Registration cancelled successfully");
            }
            return cancelled;
            
        } catch (Exception e) {
            System.err.println("Cancel registration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle Mark Attendance (Organizer only)
     * 
     * @param registrationId ID of registration
     * @return true if successful
     */
    public boolean handleMarkAttendance(String registrationId) {
        try {
            if (!SessionManager.isLoggedIn()) {
                System.err.println("Mark attendance failed: user not logged in");
                return false;
            }

            String organizerId = SessionManager.getCurrentUser().getUserId();

            boolean marked = registrationService.markAttended(registrationId, organizerId);
            
            if (marked) {
                System.out.println("Attendance marked successfully");
            }
            return marked;
            
        } catch (Exception e) {
            System.err.println("Mark attendance failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user is registered for event
     */
    public boolean isRegisteredForEvent(String eventId) {
        try {
            if (!SessionManager.isLoggedIn()) return false;
            String participantId = SessionManager.getCurrentUser().getUserId();
            return registrationService.isRegisteredForEvent(participantId, eventId);
        } catch (Exception e) {
            System.err.println("Check registration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get registration for current user and event
     */
    public Registration handleGetRegistrationForEvent(String eventId) {
        try {
            if (!SessionManager.isLoggedIn()) return null;
            String participantId = SessionManager.getCurrentUser().getUserId();
            return registrationService.getRegistrationForEvent(participantId, eventId);
        } catch (Exception e) {
            System.err.println("Get registration failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * UC8: Handle View Upcoming Registrations
     */
    public List<Registration> handleViewUpcomingRegistrations() {
        try {
            if (!SessionManager.isLoggedIn()) return List.of();
            String participantId = SessionManager.getCurrentUser().getUserId();
            return registrationService.getUpcomingRegistrations(participantId);
        } catch (Exception e) {
            System.err.println("View upcoming registrations failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Handle View Past Registrations
     */
    public List<Registration> handleViewPastRegistrations() {
        try {
            if (!SessionManager.isLoggedIn()) return List.of();
            String participantId = SessionManager.getCurrentUser().getUserId();
            return registrationService.getPastRegistrations(participantId);
        } catch (Exception e) {
            System.err.println("View past registrations failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Handle Get Event Registrations (Organizer only)
     */
    public List<Registration> handleGetEventRegistrations(String eventId) {
        try {
            if (!SessionManager.isLoggedIn()) return List.of();
            String organizerId = SessionManager.getCurrentUser().getUserId();
            return registrationService.getEventRegistrations(eventId, organizerId);
        } catch (Exception e) {
            System.err.println("Get event registrations failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Handle Get Public Event Registrations
     */
    public List<Registration> handleGetPublicEventRegistrations(String eventId) {
        try {
            return registrationService.getPublicEventRegistrations(eventId);
        } catch (Exception e) {
            System.err.println("Get public event registrations failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Cancel registration for current user and event
     */
    public boolean handleCancelRegistrationForEvent(String eventId) {
        try {
            if (!SessionManager.isLoggedIn()) return false;
            
            Registration registration = handleGetRegistrationForEvent(eventId);
            if (registration == null) {
                throw new IllegalArgumentException("Registration not found");
            }
            
            return handleCancelRegistration(registration.getRegistrationId(), "Cancelled by user");
            
        } catch (Exception e) {
            System.err.println("Cancel registration for event failed: " + e.getMessage());
            return false;
        }
    }
}