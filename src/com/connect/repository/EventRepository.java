package com.connect.repository;

import com.connect.model.Event;
import com.connect.enums.EventStatus;
import com.connect.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EventRepository - handles database operations for events.
 * 
 * GRASP Patterns:
 * - Information Expert: Knows how to persist and retrieve events
 * - Low Coupling: Database operations separated from business logic
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class EventRepository {
    
    /**
     * Create new event
     */
    public boolean createEvent(Event event) throws SQLException {
        String sql = "INSERT INTO Events (eventId, organizerId, title, description, " +
                    "startDateTime, endDateTime, venue, category, capacity, " +
                    "currentRegistrations, registrationDeadline, eventStatus, createdAt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, event.getEventId());
            ps.setString(2, event.getOrganizerId());
            ps.setString(3, event.getTitle());
            ps.setString(4, event.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(event.getStartDateTime()));
            ps.setTimestamp(6, Timestamp.valueOf(event.getEndDateTime()));
            ps.setString(7, event.getVenue());
            ps.setString(8, event.getCategory());
            ps.setInt(9, event.getCapacity());
            ps.setInt(10, event.getCurrentRegistrations());
            ps.setTimestamp(11, Timestamp.valueOf(event.getRegistrationDeadline()));
            ps.setString(12, event.getEventStatus().name());
            ps.setTimestamp(13, Timestamp.valueOf(event.getCreatedAt()));
            
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Count events created by an organizer
     */
    public int countByOrganizerId(String organizerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Events WHERE organizerId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, organizerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }
    
    /**
     * Find event by ID
     */
    public Event findById(String eventId) throws SQLException {
        String sql = "SELECT * FROM Events WHERE eventId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, eventId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }
            return null;
        }
    }
    
    /**
     * Update event
     */
    public boolean updateEvent(Event event) throws SQLException {
        String sql = "UPDATE Events SET title = ?, description = ?, startDateTime = ?, " +
                    "endDateTime = ?, venue = ?, category = ?, capacity = ?, " +
                    "currentRegistrations = ?, registrationDeadline = ?, eventStatus = ?, " +
                    "approvedBy = ?, rejectionReason = ? WHERE eventId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getStartDateTime()));
            ps.setTimestamp(4, Timestamp.valueOf(event.getEndDateTime()));
            ps.setString(5, event.getVenue());
            ps.setString(6, event.getCategory());
            ps.setInt(7, event.getCapacity());
            ps.setInt(8, event.getCurrentRegistrations());
            ps.setTimestamp(9, Timestamp.valueOf(event.getRegistrationDeadline()));
            ps.setString(10, event.getEventStatus().name());
            ps.setString(11, event.getApprovedBy());
            ps.setString(12, event.getRejectionReason());
            ps.setString(13, event.getEventId());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Update only event status (for automatic status updates)
     */
    public boolean updateEventStatus(String eventId, EventStatus status) throws SQLException {
        String sql = "UPDATE Events SET eventStatus = ? WHERE eventId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.name());
            ps.setString(2, eventId);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Find events by status
     */
    public List<Event> findByStatus(EventStatus status) throws SQLException {
        String sql = "SELECT * FROM Events WHERE eventStatus = ? ORDER BY startDateTime ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            return events;
        }
    }
    
    /**
     * Find events by organizer
     */
    public List<Event> findByOrganizerId(String organizerId) throws SQLException {
        String sql = "SELECT * FROM Events WHERE organizerId = ? ORDER BY startDateTime DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, organizerId);
            ResultSet rs = ps.executeQuery();
            
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            return events;
        }
    }
    
    /**
     * Search events with filters
     */
    public List<Event> searchEvents(String titleSearch, String category, 
                                   LocalDateTime startDate, LocalDateTime endDate) 
            throws SQLException {
        
        StringBuilder sql = new StringBuilder("SELECT * FROM Events WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (titleSearch != null && !titleSearch.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + titleSearch + "%");
        }
        
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        if (startDate != null) {
            sql.append(" AND startDateTime >= ?");
            params.add(Timestamp.valueOf(startDate));
        }
        
        if (endDate != null) {
            sql.append(" AND endDateTime <= ?");
            params.add(Timestamp.valueOf(endDate));
        }
        
        // Do not force status filtering here; allow callers to request appropriate statuses
        sql.append(" ORDER BY startDateTime ASC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            return events;
        }
    }
    
    /**
     * Get all events (for admin)
     */
    public List<Event> findAll() throws SQLException {
        String sql = "SELECT * FROM Events ORDER BY createdAt DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            return events;
        }
    }
    
    /**
     * Delete event
     */
    public boolean deleteEvent(String eventId) throws SQLException {
        String sql = "DELETE FROM Events WHERE eventId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to Event object
     */
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event event = new Event(
            rs.getString("eventId"),
            rs.getString("organizerId"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getTimestamp("startDateTime").toLocalDateTime(),
            rs.getTimestamp("endDateTime").toLocalDateTime(),
            rs.getString("venue"),
            rs.getString("category"),
            rs.getInt("capacity"),
            rs.getTimestamp("registrationDeadline").toLocalDateTime()
        );
        
        event.setCurrentRegistrations(rs.getInt("currentRegistrations"));
        event.setEventStatus(EventStatus.valueOf(rs.getString("eventStatus")));
        event.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        event.setApprovedBy(rs.getString("approvedBy"));
        event.setRejectionReason(rs.getString("rejectionReason"));
        
        return event;
    }
}