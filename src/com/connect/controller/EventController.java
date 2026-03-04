package com.connect.controller;

import com.connect.model.Event;
import com.connect.service.EventService;
import com.connect.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EventController - handles event-related UI requests.
 * 
 * GRASP Patterns:
 * - Controller: Coordinates event operations between UI and services
 * - Low Coupling: Delegates business logic to EventService
 * - Information Expert: Knows which service to call
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class EventController {
    private final EventService eventService;

    public EventController() {
        this.eventService = new EventService();
    }

    /**
     * UC10: Handle Create Event
     */
    public Event handleCreateEvent(String title, String description,
                                   LocalDateTime startDateTime, LocalDateTime endDateTime,
                                   String venue, String category, int capacity,
                                   LocalDateTime registrationDeadline) {
        try {
            if (!SessionManager.isLoggedIn()) {
                System.err.println("Create event failed: user not logged in");
                return null;
            }

            String organizerId = SessionManager.getCurrentUser().getUserId();

            Event event = eventService.createEvent(organizerId, title, description,
                    startDateTime, endDateTime, venue, category, capacity, registrationDeadline);

            System.out.println("Event created: " + event.getTitle());
            return event;

        } catch (Exception e) {
            System.err.println("Create event failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * UC11: Handle Update Event
     */
    public boolean handleUpdateEvent(String eventId, String title, String description,
                                     LocalDateTime startDateTime, LocalDateTime endDateTime,
                                     String venue, String category, int capacity,
                                     LocalDateTime registrationDeadline) {
        try {
            if (!SessionManager.isLoggedIn()) {
                System.err.println("Update event failed: user not logged in");
                return false;
            }

            String organizerId = SessionManager.getCurrentUser().getUserId();

            boolean updated = eventService.updateEvent(eventId, organizerId, title, description,
                    startDateTime, endDateTime, venue, category, capacity, registrationDeadline);

            if (updated) {
                System.out.println("Event updated successfully");
            }
            return updated;

        } catch (Exception e) {
            System.err.println("Update event failed: " + e.getMessage());
            return false;
        }
    }


    /**
     * UC3: Handle Browse Events
     */
    public List<Event> handleBrowseEvents() {
        try {
            return eventService.getApprovedEvents();
        } catch (Exception e) {
            System.err.println("Browse events failed: " + e.getMessage());
            return List.of(); // Return empty list
        }
    }

    /**
     * UC3: Handle Search Events
     */
    public List<Event> handleSearchEvents(String searchQuery, String category,
                                         LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return eventService.searchEvents(searchQuery, category, startDate, endDate);
        } catch (Exception e) {
            System.err.println("Search events failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get events organized by current user
     */
    public List<Event> handleViewMyEvents() {
        try {
            if (!SessionManager.isLoggedIn()) return List.of();
            String organizerId = SessionManager.getCurrentUser().getUserId();
            return eventService.getEventsByOrganizer(organizerId);
        } catch (Exception e) {
            System.err.println("View my events failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get single event details
     */
    public Event handleGetEventDetails(String eventId) {
        try {
            return eventService.getEventById(eventId);
        } catch (Exception e) {
            System.err.println("Get event details failed: " + e.getMessage());
            return null;
        }
    }

}