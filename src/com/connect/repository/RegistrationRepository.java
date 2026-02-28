package com.connect.repository;

import com.connect.model.Registration;
import com.connect.enums.RegistrationStatus;
import com.connect.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RegistrationRepository - handles database operations for registrations.
 * 
 * GRASP Patterns:
 * - Information Expert: Knows how to persist and retrieve registrations
 * - Low Coupling: Database operations separated from business logic
 * 
 * @author Obaidullah Shoaib (23i-0609)
 */
public class RegistrationRepository {
    
    /**
     * Create new registration
     */
    public boolean createRegistration(Registration registration) throws SQLException {
        String sql = "INSERT INTO Registrations (registrationId, participantId, eventId, " +
                    "registrationDate, status, attended) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, registration.getRegistrationId());
            ps.setString(2, registration.getParticipantId());
            ps.setString(3, registration.getEventId());
            ps.setTimestamp(4, Timestamp.valueOf(registration.getRegistrationDate()));
            ps.setString(5, registration.getStatus().name());
            ps.setBoolean(6, registration.isAttended());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Find registration by ID
     */
    public Registration findById(String registrationId) throws SQLException {
        String sql = "SELECT * FROM Registrations WHERE registrationId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, registrationId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRegistration(rs);
            }
            return null;
        }
    }
    
    /**
     * Find registration by participant and event
     */
    public Registration findByParticipantAndEvent(String participantId, String eventId) throws SQLException {
        String sql = "SELECT * FROM Registrations WHERE participantId = ? AND eventId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, participantId);
            ps.setString(2, eventId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRegistration(rs);
            }
            return null;
        }
    }
    
    /**
     * Update registration
     */
    public boolean updateRegistration(Registration registration) throws SQLException {
        String sql = "UPDATE Registrations SET status = ?, attended = ?, " +
                    "cancelledAt = ?, cancellationReason = ? WHERE registrationId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, registration.getStatus().name());
            ps.setBoolean(2, registration.isAttended());
            
            if (registration.getCancelledAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(registration.getCancelledAt()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            
            ps.setString(4, registration.getCancellationReason());
            ps.setString(5, registration.getRegistrationId());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Find upcoming registrations for participant
     */
    public List<Registration> findUpcomingByParticipant(String participantId) throws SQLException {
        String sql = "SELECT r.* FROM Registrations r " +
                    "JOIN Events e ON r.eventId = e.eventId " +
                    "WHERE r.participantId = ? AND r.status = 'CONFIRMED' " +
                    "AND e.startDateTime > ? " +
                    "ORDER BY e.startDateTime ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, participantId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            
            List<Registration> registrations = new ArrayList<>();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
            return registrations;
        }
    }
    
    /**
     * Find past registrations for participant
     */
    public List<Registration> findPastByParticipant(String participantId) throws SQLException {
        String sql = "SELECT r.* FROM Registrations r " +
                    "JOIN Events e ON r.eventId = e.eventId " +
                    "WHERE r.participantId = ? AND r.status = 'CONFIRMED' " +
                    "AND e.endDateTime < ? " +
                    "ORDER BY e.startDateTime DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, participantId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            
            List<Registration> registrations = new ArrayList<>();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
            return registrations;
        }
    }
    
    /**
     * Find registrations for event
     */
    public List<Registration> findByEventId(String eventId) throws SQLException {
        String sql = "SELECT * FROM Registrations WHERE eventId = ? ORDER BY registrationDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, eventId);
            ResultSet rs = ps.executeQuery();
            
            List<Registration> registrations = new ArrayList<>();
            while (rs.next()) {
            	System.out.println("Found registration: " + rs.getString("registrationId"));
                registrations.add(mapResultSetToRegistration(rs));
            }
            return registrations;
        }
    }
    
    /**
     * Find active registrations for event (public view)
     */
    public List<Registration> findActiveByEventId(String eventId) throws SQLException {
        String sql = "SELECT * FROM Registrations WHERE eventId = ? AND status = 'CONFIRMED' " +
                    "ORDER BY registrationDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, eventId);
            ResultSet rs = ps.executeQuery();
            
            List<Registration> registrations = new ArrayList<>();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
            return registrations;
        }
    }
    
    /**
     * Count active registrations for event
     */
    public int countActiveByEventId(String eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Registrations WHERE eventId = ? AND status = 'CONFIRMED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, eventId);
            ResultSet rs = ps.executeQuery();
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Count attended events by participant
     */
    public int countAttendedByParticipant(String participantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Registrations WHERE participantId = ? AND attended = true AND status = 'CONFIRMED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, participantId);
            ResultSet rs = ps.executeQuery();
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Map ResultSet to Registration object
     */
    private Registration mapResultSetToRegistration(ResultSet rs) throws SQLException {
        Registration registration = new Registration(
            rs.getString("registrationId"),
            rs.getString("participantId"),
            rs.getString("eventId")
        );
        
        registration.setRegistrationDate(rs.getTimestamp("registrationDate").toLocalDateTime());
        registration.setStatus(RegistrationStatus.valueOf(rs.getString("status")));
        registration.setAttended(rs.getBoolean("attended"));
        
        Timestamp cancelledAt = rs.getTimestamp("cancelledAt");
        if (cancelledAt != null) {
            registration.setCancelledAt(cancelledAt.toLocalDateTime());
        }
        
        registration.setCancellationReason(rs.getString("cancellationReason"));
        
        return registration;
    }
}