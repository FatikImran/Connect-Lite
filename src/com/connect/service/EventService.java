package com.connect.service;

import com.connect.model.*;
import com.connect.repository.EventRepository;
import com.connect.repository.UserRepository;
import com.connect.util.IdGenerator;
import com.connect.util.ValidationUtil;
import com.connect.enums.EventStatus;
import com.connect.enums.UserType;

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

}