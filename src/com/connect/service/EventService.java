package com.connect.service;

import com.connect.model.*;
import com.connect.repository.EventRepository;
import com.connect.repository.UserRepository;
import com.connect.util.IdGenerator;
import com.connect.enums.EventStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EventService - handles all event-related business logic.
 * 
 * GRASP Patterns:
 * - Information Expert: Encapsulates event business rules
 * - Controller: Coordinates event operations
 * - High Cohesion: All event logic together
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService() {
        this.eventRepository = new EventRepository();
        this.userRepository = new UserRepository();
    }

    /**
     * UC10: Create Event
     */
    public Event createEvent(String organizerId, String title, String description,
                            LocalDateTime startDateTime, LocalDateTime endDateTime,
                            String venue, String category, int capacity,
                            LocalDateTime registrationDeadline)
            throws SQLException, IllegalArgumentException {

        User organizer = userRepository.findById(organizerId);
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found");
        }
        if (!organizer.isActive()) {
            throw new IllegalArgumentException("Account is not active");
        }

        validateEventData(title, description, startDateTime, endDateTime,
                venue, capacity, registrationDeadline);

        String eventId = IdGenerator.generateEventId();

        Event event = new Event(eventId, organizerId, title, description,
                startDateTime, endDateTime, venue, category,
                capacity, registrationDeadline);

        boolean saved = eventRepository.createEvent(event);
        if (!saved) {
            throw new SQLException("Failed to create event");
        }

        return event;
    }

    /**
     * Ensure the event status stored in DB reflects the current time.
     * If an event's start/end times indicate it should be ONGOING or COMPLETED,
     * update the status in the Event object and persist the change.
     */
    private void syncEventStatus(Event event) {
        if (event == null) return;

        LocalDateTime now = LocalDateTime.now();
        EventStatus currentStatus = event.getEventStatus();
        EventStatus desiredStatus = currentStatus;

        try {
            // Only update if event is not in terminal states
            if (currentStatus != EventStatus.CANCELLED && 
                currentStatus != EventStatus.REJECTED &&
                currentStatus != EventStatus.COMPLETED) {
                
                if (event.getEndDateTime() != null && now.isAfter(event.getEndDateTime())) {
                    desiredStatus = EventStatus.COMPLETED;
                } else if (event.getStartDateTime() != null && 
                          now.isAfter(event.getStartDateTime()) &&
                          event.getEndDateTime() != null && 
                          now.isBefore(event.getEndDateTime())) {
                    desiredStatus = EventStatus.ONGOING;
                } else if (event.getStartDateTime() != null && 
                          now.isBefore(event.getStartDateTime()) &&
                          currentStatus == EventStatus.APPROVED) {
                    // Event is approved but hasn't started yet - keep as APPROVED
                    desiredStatus = EventStatus.APPROVED;
                }
            }

            // Update if status changed
            if (desiredStatus != currentStatus) {
                event.setEventStatus(desiredStatus);
                // Persist update
                try {
                    eventRepository.updateEventStatus(event.getEventId(), desiredStatus);
                    System.out.println("🔄 Event status updated: " + event.getEventId() + 
                                     " from " + currentStatus + " to " + desiredStatus);
                } catch (Exception ex) {
                    System.err.println("⚠ Failed to persist event status update for " + 
                                    event.getEventId() + ": " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("⚠ Error while syncing event status: " + ex.getMessage());
        }
    }


    /**
     * UC3: Browse Events - Get all approved events
     */
    public List<Event> getApprovedEvents() throws SQLException {
        List<Event> events = eventRepository.findByStatus(EventStatus.APPROVED);
        // Also include ongoing events in browse view
        List<Event> ongoingEvents = eventRepository.findByStatus(EventStatus.ONGOING);
        
        // Combine and sync statuses
        events.addAll(ongoingEvents);
        for (Event e : events) syncEventStatus(e);
        return events;
    }

    /**
     * UC3: Browse Events - Search with filters
     */
    public List<Event> searchEvents(String titleSearch, String category, 
                                   LocalDateTime startDate, LocalDateTime endDate) 
            throws SQLException {
        List<Event> results = eventRepository.searchEvents(titleSearch, category, startDate, endDate);
        for (Event e : results) syncEventStatus(e);
        return results;
    }

    /**
     * Get events organized by specific user
     */
    public List<Event> getEventsByOrganizer(String organizerId) throws SQLException {
        List<Event> results = eventRepository.findByOrganizerId(organizerId);
        for (Event e : results) syncEventStatus(e);
        return results;
    }

    /**
     * Get single event by ID
     */
    public Event getEventById(String eventId) throws SQLException {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        // Ensure stored status matches current time
        syncEventStatus(event);
        return event;
    }

    /**
     * Update event (organizer)
     */
    public boolean updateEvent(String eventId, String organizerId, String title, String description,
                               LocalDateTime startDateTime, LocalDateTime endDateTime,
                               String venue, String category, int capacity,
                               LocalDateTime registrationDeadline) throws SQLException {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalArgumentException("Unauthorized: Only organizer can update event");
        }

        if (!event.canBeUpdated()) {
            throw new IllegalArgumentException("This event can no longer be updated");
        }

        validateEventData(title, description, startDateTime, endDateTime,
                venue, capacity, registrationDeadline);

        event.setTitle(title);
        event.setDescription(description);
        event.setStartDateTime(startDateTime);
        event.setEndDateTime(endDateTime);
        event.setVenue(venue);
        event.setCategory(category);
        event.setCapacity(capacity);
        event.setRegistrationDeadline(registrationDeadline);

        boolean updated = eventRepository.updateEvent(event);
        if (updated) {
            syncEventStatus(event);
        }
        return updated;
    }

    /**
     * Cancel event (organizer)
     */
    public boolean cancelEvent(String eventId, String organizerId, String reason) throws SQLException {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalArgumentException("Unauthorized: Only organizer can cancel event");
        }

        if (!event.canBeCancelled()) {
            throw new IllegalArgumentException("This event can no longer be cancelled");
        }

        event.cancel(reason == null || reason.isBlank() ? "Cancelled by organizer" : reason);
        return eventRepository.updateEvent(event);
    }

    /**
     * Un-cancel event (organizer)
     */
    public boolean uncancelEvent(String eventId, String organizerId) throws SQLException {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalArgumentException("Unauthorized: Only organizer can uncancel event");
        }

        if (event.getEventStatus() != EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Event is not cancelled");
        }

        if (event.getStartDateTime() != null && LocalDateTime.now().isAfter(event.getStartDateTime())) {
            throw new IllegalArgumentException("Cannot uncancel an event that has already started");
        }

        event.setEventStatus(EventStatus.PENDING_APPROVAL);
        event.setRejectionReason(null);
        event.setApprovedBy(null);

        return eventRepository.updateEvent(event);
    }

    /**
     * Delete event (organizer)
     */
    public boolean deleteEvent(String eventId, String organizerId) throws SQLException {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalArgumentException("Unauthorized: Only organizer can delete event");
        }

        if (event.getCurrentRegistrations() > 0) {
            throw new IllegalArgumentException("Cannot delete event with active registrations");
        }

        return eventRepository.deleteEvent(eventId);
    }


    /**
     * Update all event statuses (background task)
     */
    public void updateAllEventStatuses() throws SQLException {
        List<Event> allEvents = eventRepository.findAll();
        for (Event event : allEvents) {
            try {
                syncEventStatus(event);
            } catch (Exception ex) {
                System.err.println("⚠ Failed to sync status for event " + 
                                event.getEventId() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Backwards-compatible wrapper invoked by scheduler.
     */
    public void updateEventStatuses() throws SQLException {
        updateAllEventStatuses();
    }

    /**
     * Validate event data
     */
    private void validateEventData(String title, String description,
                                   LocalDateTime startDateTime, LocalDateTime endDateTime,
                                   String venue, int capacity,
                                   LocalDateTime registrationDeadline)
            throws IllegalArgumentException {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title is required");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title too long (max 200 characters)");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Event description is required");
        }

        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue is required");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        LocalDateTime now = LocalDateTime.now();

        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Start and end date/time are required");
        }

        if (startDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Event cannot start in the past");
        }

        if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (registrationDeadline == null) {
            throw new IllegalArgumentException("Registration deadline is required");
        }

        if (registrationDeadline.isAfter(startDateTime)) {
            throw new IllegalArgumentException("Registration deadline must be before event start time");
        }

        if (registrationDeadline.isBefore(now)) {
            throw new IllegalArgumentException("Registration deadline cannot be in the past");
        }
    }

}