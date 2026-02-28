package com.connect.service;

import com.connect.model.Registration;
import com.connect.model.Event;
import com.connect.model.User;
import com.connect.repository.RegistrationRepository;
import com.connect.repository.EventRepository;
import com.connect.repository.UserRepository;
import com.connect.util.IdGenerator;
import com.connect.util.SessionManager;
import com.connect.enums.RegistrationStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RegistrationService - handles all registration-related business logic.
 * 
 * GRASP Patterns:
 * - Information Expert: Encapsulates registration business rules
 * - Controller: Coordinates registration operations
 * - High Cohesion: All registration logic together
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService() {
        this.registrationRepository = new RegistrationRepository();
        this.eventRepository = new EventRepository();
        this.userRepository = new UserRepository();
    }

    /**
     * Register participant for an event with comprehensive validation
     * 
     * @param participantId ID of the participant
     * @param eventId ID of the event
     * @return Created registration
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if validation fails
     */
    public Registration registerForEvent(String participantId, String eventId) 
            throws SQLException, IllegalArgumentException {
        
        // 1. Validate participant exists and is active
        User participant = userRepository.findById(participantId);
        if (participant == null) {
            throw new IllegalArgumentException("Participant not found");
        }
        
        if (!participant.isActive()) {
            throw new IllegalArgumentException("Participant account is not active");
        }
        
        // 2. Validate event exists and is approved
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        
        if (event.getEventStatus() != com.connect.enums.EventStatus.APPROVED) {
            throw new IllegalArgumentException("Event is not available for registration");
        }
        
        // 3. Check if participant is the organizer
        if (event.isOrganizer(participantId)) {
            throw new IllegalArgumentException("Organizers cannot register for their own events");
        }
        
        // 4. Check if registration is still open
        if (!event.isRegistrationOpen()) {
            throw new IllegalArgumentException("Registration is closed for this event");
        }
        
        // 5. Check if event has capacity
        if (!event.hasCapacity()) {
            throw new IllegalArgumentException("Event is full");
        }
        
        // 6. Check if participant is already registered
        Registration existingRegistration = registrationRepository.findByParticipantAndEvent(participantId, eventId);
        if (existingRegistration != null) {
            if (existingRegistration.isActive()) {
                throw new IllegalArgumentException("You are already registered for this event");
            } else {
                throw new IllegalArgumentException("Your previous registration was cancelled");
            }
        }
        
        // 7. Create and save registration
        String registrationId = IdGenerator.generateRegistrationId();
        Registration registration = new Registration(registrationId, participantId, eventId);
        
        boolean saved = registrationRepository.createRegistration(registration);
        if (!saved) {
            throw new SQLException("Failed to save registration");
        }
        
        return registration;
    }

    /**
     * Cancel registration with validation
     * 
     * @param registrationId ID of the registration to cancel
     * @param reason Reason for cancellation
     * @return true if cancellation successful
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if validation fails
     */
    public boolean cancelRegistration(String registrationId, String reason) 
            throws SQLException, IllegalArgumentException {
        
        Registration registration = registrationRepository.findById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("Registration not found");
        }
        
        // Check if registration can be cancelled
        if (!registration.canBeCancelled()) {
            throw new IllegalArgumentException("Registration cannot be cancelled");
        }
        
        // Check if current user is the participant
        String currentUserId = SessionManager.getCurrentUserId();
        if (!registration.getParticipantId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only cancel your own registrations");
        }
        
        registration.cancel(reason);
        return registrationRepository.updateRegistration(registration);
    }

    /**
     * Mark participant as attended (Organizer only)
     * 
     * @param registrationId ID of the registration
     * @param organizerId ID of the organizer
     * @return true if successful
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if validation fails
     */
    public boolean markAttended(String registrationId, String organizerId) 
            throws SQLException, IllegalArgumentException {
        
        Registration registration = registrationRepository.findById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("Registration not found");
        }
        
        Event event = eventRepository.findById(registration.getEventId());
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        
        // Verify organizer access
        if (!event.isOrganizer(organizerId)) {
            throw new IllegalArgumentException("Only event organizer can mark attendance");
        }
        
        // Check if event is completed or ongoing
        if (!event.isCompleted() && !event.isOngoing()) {
            throw new IllegalArgumentException("Attendance can only be marked for ongoing or completed events");
        }
        
        registration.markAttended();
        return registrationRepository.updateRegistration(registration);
    }

    /**
     * Check if user is registered for an event
     */
    public boolean isRegisteredForEvent(String participantId, String eventId) throws SQLException {
        Registration registration = registrationRepository.findByParticipantAndEvent(participantId, eventId);
        return registration != null && registration.isActive();
    }

    /**
     * Get registration for participant and event
     */
    public Registration getRegistrationForEvent(String participantId, String eventId) throws SQLException {
        return registrationRepository.findByParticipantAndEvent(participantId, eventId);
    }

    /**
     * Get upcoming registrations for participant
     */
    public List<Registration> getUpcomingRegistrations(String participantId) throws SQLException {
        return registrationRepository.findUpcomingByParticipant(participantId);
    }

    /**
     * Get past registrations for participant
     */
    public List<Registration> getPastRegistrations(String participantId) throws SQLException {
        return registrationRepository.findPastByParticipant(participantId);
    }

    /**
     * Get registrations for event (Organizer view)
     */
    public List<Registration> getEventRegistrations(String eventId, String organizerId) 
            throws SQLException, IllegalArgumentException {
        
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        
        if (!event.isOrganizer(organizerId)) {
            throw new IllegalArgumentException("Only event organizer can view registrations");
        }
        
        return registrationRepository.findByEventId(eventId);
    }

    /**
     * Get public registrations for event (limited info)
     */
    public List<Registration> getPublicEventRegistrations(String eventId) throws SQLException {
        return registrationRepository.findActiveByEventId(eventId);
    }
}