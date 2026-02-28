package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import com.connect.util.NavigationUtil;
import com.connect.util.SessionManager;
import com.connect.util.DataReceiver;
import com.connect.controller.EventController;
import com.connect.controller.RegistrationController;
import com.connect.controller.UserController;
import com.connect.enums.EventStatus;
import com.connect.model.Event;
import com.connect.model.User;
import java.time.format.DateTimeFormatter;

public class EventDetailsController implements DataReceiver {
    
    // Navigation buttons
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button backButton;
    
    // Event details
    @FXML private ImageView eventImageView;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventStatusLabel;
    @FXML private Label venueLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label participantsLabel;
    @FXML private Label categoryLabel;
    @FXML private Hyperlink organizerLink;
    @FXML private Label descriptionLabel;
    
    // Action buttons
    @FXML private Button registerButton;
    @FXML private Button cancelRegistrationButton;
    @FXML private Button editEventButton;
    
    // Deadline warning
    @FXML private HBox deadlineWarning;
    @FXML private Label deadlineLabel;
    
    // Participants section
    @FXML private Label participantCountLabel;
    @FXML private FlowPane participantsFlow;
    @FXML private Hyperlink viewAllParticipantsLink;

    // Backend controllers
    private final EventController eventController = new EventController();
    private final RegistrationController registrationController = new RegistrationController();
    private final UserController userController = new UserController();

    private Event currentEvent;
    private boolean isRegistered = false;
    private boolean hasAttended = false;
    private String eventId;
    
    @FXML
    public void initialize() {
        System.out.println("✅ EventDetailsController initialized!");

        if (logoutButton != null) logoutButton.setOnAction(e -> handleLogout());
        if (backButton != null) backButton.setOnAction(e -> handleBack());

        // Load event details - eventId may be set later via receiveData/setEventId
        loadEventDetails();
    }
    
    /**
     * Method to receive event ID from calling screen
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
        loadEventDetails();
    }

    @Override
    public void receiveData(Object data) {
        if (data == null) return;

        // If caller passed a raw Event object
        if (data instanceof Event) {
            this.currentEvent = (Event) data;
            this.eventId = currentEvent.getEventId();
            displayEventDetails();
            checkRegistrationStatus();
            return;
        }

        // If caller passed the event id as String
        if (data instanceof String) {
            setEventId((String) data);
            return;
        }

        // Fallback: try to use toString()
        setEventId(data.toString());
    }
    
    // ==================== DATA LOADING ====================
    
    private void loadEventDetails() {
        System.out.println("📊 Loading event details from database...");
        
        // For demo purposes, if eventId is not set, show placeholder
        if (eventId == null || eventId.isEmpty()) {
            showPlaceholder();
            return;
        }
        
        try {
            currentEvent = eventController.handleGetEventDetails(eventId);
            
            if (currentEvent != null) {
                displayEventDetails();
                checkRegistrationStatus();
                checkAttendanceStatus();
                loadParticipants();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Event not found");
                showPlaceholder();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading event details: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load event details");
            showPlaceholder();
        }
    }
    
    private void displayEventDetails() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        if (eventTitleLabel != null) {
            eventTitleLabel.setText(currentEvent.getTitle());
        }
        if (venueLabel != null) {
            venueLabel.setText(currentEvent.getVenue());
        }
        if (dateLabel != null) {
            dateLabel.setText(currentEvent.getStartDateTime().format(dateFormatter));
        }
        if (timeLabel != null) {
            timeLabel.setText(currentEvent.getStartDateTime().format(timeFormatter) + " - " + 
                            currentEvent.getEndDateTime().format(timeFormatter));
        }
        if (participantsLabel != null) {
            participantsLabel.setText(currentEvent.getCurrentRegistrations() + "/" + 
                                    currentEvent.getCapacity() + " Participants");
        }
        if (categoryLabel != null) {
            categoryLabel.setText(currentEvent.getCategory());
        }
        if (descriptionLabel != null) {
            descriptionLabel.setText(currentEvent.getDescription());
        }
        if (eventStatusLabel != null) {
            eventStatusLabel.setText(currentEvent.getEventStatus().toString());
            // Update status label color based on status
            String statusStyle = getStatusStyle(currentEvent.getEventStatus());
            eventStatusLabel.setStyle(statusStyle);
        }

        // Set button visibility based on user role and registration status
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean isOrganizer = false;
        try {
            String currentUserId = SessionManager.getCurrentUserId();
            isOrganizer = currentEvent != null && currentUserId != null &&
                         currentEvent.getOrganizerId().equals(currentUserId);
        } catch (Exception e) {
            System.err.println("❌ Error checking organizer status: " + e.getMessage());
        }
        
        if (registerButton != null) {
            boolean canRegister = !isRegistered && !isOrganizer &&
                                currentEvent.isRegistrationOpen() &&
                                currentEvent.hasCapacity();
            registerButton.setVisible(canRegister);
            registerButton.setManaged(canRegister);
            registerButton.setDisable(!canRegister);
        }
        if (cancelRegistrationButton != null) {
            cancelRegistrationButton.setVisible(isRegistered && !isOrganizer);
            cancelRegistrationButton.setManaged(isRegistered && !isOrganizer);
        }
        if (editEventButton != null) {
            editEventButton.setVisible(isOrganizer);
            editEventButton.setManaged(isOrganizer);
        }
    }

    /**
     * Check if current user is registered for this event
     */
    private void checkRegistrationStatus() {
        try {
            isRegistered = registrationController.isRegisteredForEvent(currentEvent.getEventId());
            updateButtonVisibility();
        } catch (Exception e) {
            System.err.println("❌ Error checking registration status: " + e.getMessage());
        }
    }

	 /**
     * Check if current user attended this event
     */
    private void checkAttendanceStatus() {
        try {
            var registration = registrationController.handleGetRegistrationForEvent(currentEvent.getEventId());
            hasAttended = registration != null && registration.isAttended();
        } catch (Exception e) {
            System.err.println("❌ Error checking attendance status: " + e.getMessage());
            hasAttended = false;
        }
    }

    private void loadParticipants() {
        System.out.println("👥 Loading participants...");
        
        if (currentEvent == null) return;

        try {
            java.util.List<com.connect.model.Registration> regs = registrationController.handleGetPublicEventRegistrations(currentEvent.getEventId());

            // Filter out cancelled registrations for public display
            java.util.List<com.connect.model.Registration> visibleRegs = regs.stream()
                    .filter(r -> r.getStatus() != com.connect.enums.RegistrationStatus.CANCELLED)
                    .toList();

            if (participantCountLabel != null) {
                participantCountLabel.setText("(" + visibleRegs.size() + ")");
            }

            if (participantsFlow != null) {
                participantsFlow.getChildren().clear();

                for (com.connect.model.Registration r : visibleRegs) {
                    String participantId = r.getParticipantId();

                    // Try to resolve participant name, fallback to id
                    String name = participantId;
                    try {
                        User u = userController.handleGetUserProfile(participantId);
                        if (u != null && u.getName() != null && !u.getName().isEmpty()) {
                            name = u.getName();
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Failed to load user for participant " + participantId + ": " + e.getMessage());
                    }

                    VBox card = new VBox(4);
                    card.setStyle("-fx-alignment: center; -fx-padding: 6;");

                    // Simple circle placeholder for avatar
                    javafx.scene.shape.Circle avatar = new javafx.scene.shape.Circle(20);
                    avatar.setStyle("-fx-fill: #e0e7ff;");

                    Label nameLabel = new Label(name);
                    nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

                    card.getChildren().addAll(avatar, nameLabel);
                    participantsFlow.getChildren().add(card);
                }

                // Hide the 'view all' link since we display full list inline
                if (viewAllParticipantsLink != null) {
                    viewAllParticipantsLink.setVisible(false);
                    viewAllParticipantsLink.setManaged(false);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading participants: " + e.getMessage());
            if (participantCountLabel != null) {
                participantCountLabel.setText("(" + currentEvent.getCurrentRegistrations() + ")");
            }
        }
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleRegister() {
        System.out.println("✅ Registering for event...");
        
        if (currentEvent == null) return;
        
        // Check if event is full
        if (currentEvent.getCurrentRegistrations() >= currentEvent.getCapacity()) {
            showAlert(Alert.AlertType.WARNING, "Event Full", 
                "Sorry, this event has reached maximum capacity.");
            return;
        }
        
        try {
            var registration = registrationController.handleRegisterForEvent(currentEvent.getEventId());
            
            if (registration != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Successfully registered for the event!");
                
                // Update registration status and reload details
                isRegistered = true;
                loadEventDetails();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register for event");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error registering for event: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelRegistration() {
        System.out.println("❌ Canceling registration...");
        
        if (currentEvent == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Registration");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to cancel your registration?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Get the actual registration for current user + event
                    var reg = registrationController.handleGetRegistrationForEvent(currentEvent.getEventId());
                    if (reg == null) {
                        showAlert(Alert.AlertType.WARNING, "Not Registered", "You are not registered for this event.");
                        return;
                    }

                    boolean cancelled = registrationController.handleCancelRegistration(reg.getRegistrationId(), "Cancelled by user");
                    if (cancelled) {
                        showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Registration cancelled successfully!");
                        isRegistered = false;
                        loadEventDetails();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel registration. Please try again later.");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error cancelling registration: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel registration: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleEditEvent() {
        System.out.println("✏️ Editing event...");
        // TODO: Navigate to edit event screen with event data
        showAlert(Alert.AlertType.INFORMATION, "Edit Event", "Edit functionality coming soon!");
    }
    
    @FXML
    private void handleViewOrganizer() {
        System.out.println("👤 Viewing organizer profile...");
        // TODO: Navigate to organizer's profile
        showAlert(Alert.AlertType.INFORMATION, "Organizer", "Organizer profile view coming soon!");
    }
    
    @FXML
    private void handleViewAllParticipants() {
        System.out.println("👥 Viewing all participants...");
        try {
            NavigationUtil.navigateToWithDataReceiver(
                "/fxml/participants-list.fxml",
                "Participants",
                viewAllParticipantsLink,
                480,
                600,
                dr -> {
                    if (dr != null) dr.receiveData(currentEvent.getEventId());
                }
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to open participants list: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open participants list.");
        }
    }

    private void showPlaceholder() {
        if (eventTitleLabel != null) eventTitleLabel.setText("Sample Event Title");
        if (venueLabel != null) venueLabel.setText("Sample Venue");
        if (dateLabel != null) dateLabel.setText("November 20, 2025");
        if (timeLabel != null) timeLabel.setText("2:00 PM - 5:00 PM");
        if (participantsLabel != null) participantsLabel.setText("0/100 Participants");
        if (categoryLabel != null) categoryLabel.setText("Technology");
        if (descriptionLabel != null) descriptionLabel.setText("Event description will be loaded here...");
        if (eventStatusLabel != null) eventStatusLabel.setText("OPEN");
        
        // Show register button by default
        updateButtonVisibility();
    }
    
    // ==================== NAVIGATION HANDLERS ====================
    
    @FXML
    private void handleBack() {
        NavigationUtil.navigateTo("/fxml/browse-events.fxml", "Connect - Browse Events", backButton, 1200, 800);
    }
    
    @FXML
    private void handleLogout() {
        NavigationUtil.navigateTo("/fxml/login.fxml", "Connect - Sign In", logoutButton, 400, 600);
    }

    // ==================== UTILITY METHODS ====================
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getStatusStyle(EventStatus status) {
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;";

        switch (status) {
            case COMPLETED:
                return baseStyle + " -fx-background-color: #33CE9A;"; // Green
            case ONGOING:
                return baseStyle + " -fx-background-color: #0678FC;"; // Blue
            case CANCELLED:
                return baseStyle + " -fx-background-color: #DC3545;"; // Red
            case PENDING_APPROVAL:
                return baseStyle + " -fx-background-color: #FFC107; -fx-text-fill: #856404;"; // Yellow
            case APPROVED:
                return baseStyle + " -fx-background-color: #33CE9A;"; // Green
            default:
                return baseStyle + " -fx-background-color: #666666;"; // Gray
        }
    }
}