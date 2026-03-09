package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.connect.util.NavigationUtil;
import com.connect.util.SessionManager;
import com.connect.controller.EventController;
import com.connect.controller.RegistrationController;
import com.connect.model.Event;
import com.connect.model.Registration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyEventsController {

    @FXML private VBox rootContainer;
    
    // Navigation buttons
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button createEventButton;
    
    // Tab buttons
    @FXML private Button attendingTabButton;
    @FXML private Button organizingTabButton;
    
    // Filter controls
    @FXML private CheckBox pastEventsCheckbox;
    @FXML private HBox organizerActionsRow;
    
    // Containers
    @FXML private VBox attendingEventsContainer;
    @FXML private ListView<Event> attendingEventsList;
    @FXML private VBox attendingEmptyState;
    
    @FXML private VBox organizingEventsContainer;
    @FXML private ListView<Event> organizingEventsList;
    @FXML private VBox organizingEmptyState;
    
    // Backend controllers
    private final RegistrationController registrationController = new RegistrationController();
    private final EventController eventController = new EventController();
    
    private boolean isAttendingTabActive = true;
    private ObservableList<Event> attendingEvents;
    private ObservableList<Event> organizingEvents;
    
    @FXML
    public void initialize() {
        System.out.println("✅ MyEventsController initialized!");
        
        // Set up event handlers
        if (logoutButton != null) logoutButton.setOnAction(e -> handleLogout());
        if (profileButton != null) profileButton.setOnAction(e -> handleProfile());
        if (createEventButton != null) createEventButton.setOnAction(e -> handleCreateEvent());
        if (pastEventsCheckbox != null) pastEventsCheckbox.setOnAction(e -> handleTogglePastEvents());
        
        // Setup list views
        setupListViews();
        
        // Show attending tab by default
        showAttendingTab();
        
        // Load events
        loadEvents();
        updateOrganizerActionVisibility();
    }
    
    private void setupListViews() {
        if (attendingEventsList != null) {
            attendingEventsList.setCellFactory(lv -> new EventListCell());
            attendingEventsList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    handleViewEvent();
                }
            });
        }
        
        if (organizingEventsList != null) {
            organizingEventsList.setCellFactory(lv -> new EventListCell());
            organizingEventsList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    handleViewEvent();
                }
            });
        }
    }
    
    // ==================== TAB SWITCHING ====================
    
    @FXML
    private void handleAttendingTab() {
        System.out.println("📅 Switching to Attending tab");
        isAttendingTabActive = true;
        showAttendingTab();
        loadEvents();
    }
    
    @FXML
    private void handleOrganizingTab() {
        System.out.println("🎯 Switching to Organizing tab");
        isAttendingTabActive = false;
        showOrganizingTab();
        loadEvents();
    }
    
    private void showAttendingTab() {
        // Update tab button styles
        if (attendingTabButton != null) {
            attendingTabButton.setStyle(
                "-fx-background-color: #0678FC; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 12 30; " +
                "-fx-border-radius: 8 0 0 0; " +
                "-fx-background-radius: 8 0 0 0; " +
                "-fx-cursor: hand;"
            );
        }
        if (organizingTabButton != null) {
            organizingTabButton.setStyle(
                "-fx-background-color: #E0E0E0; " +
                "-fx-text-fill: #666666; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 12 30; " +
                "-fx-border-radius: 0 8 0 0; " +
                "-fx-background-radius: 0 8 0 0; " +
                "-fx-cursor: hand;"
            );
        }
        
        // Show/hide containers
        if (attendingEventsContainer != null) {
            attendingEventsContainer.setVisible(true);
            attendingEventsContainer.setManaged(true);
        }
        if (organizingEventsContainer != null) {
            organizingEventsContainer.setVisible(false);
            organizingEventsContainer.setManaged(false);
        }

        updateOrganizerActionVisibility();
    }
    
    private void showOrganizingTab() {
        // Update tab button styles
        if (organizingTabButton != null) {
            organizingTabButton.setStyle(
                "-fx-background-color: #0678FC; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 12 30; " +
                "-fx-border-radius: 0 8 0 0; " +
                "-fx-background-radius: 0 8 0 0; " +
                "-fx-cursor: hand;"
            );
        }
        if (attendingTabButton != null) {
            attendingTabButton.setStyle(
                "-fx-background-color: #E0E0E0; " +
                "-fx-text-fill: #666666; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 12 30; " +
                "-fx-border-radius: 8 0 0 0; " +
                "-fx-background-radius: 8 0 0 0; " +
                "-fx-cursor: hand;"
            );
        }
        
        // Show/hide containers
        if (attendingEventsContainer != null) {
            attendingEventsContainer.setVisible(false);
            attendingEventsContainer.setManaged(false);
        }
        if (organizingEventsContainer != null) {
            organizingEventsContainer.setVisible(true);
            organizingEventsContainer.setManaged(true);
        }

        updateOrganizerActionVisibility();
    }
    
    // ==================== DATA LOADING ====================
    
    private void loadEvents() {
        System.out.println("📊 Loading " + (isAttendingTabActive ? "attending" : "organizing") + " events...");
        
        // Interpret checkbox as "Show only past events" when checked
        boolean showOnlyPast = pastEventsCheckbox != null && pastEventsCheckbox.isSelected();
        System.out.println("Show only past events: " + showOnlyPast);
        
        if (isAttendingTabActive) {
            loadAttendingEvents(showOnlyPast);
        } else {
            loadOrganizingEvents(showOnlyPast);
        }

        updateOrganizerActionVisibility();
    }

    private void updateOrganizerActionVisibility() {
        boolean showOnlyPast = pastEventsCheckbox != null && pastEventsCheckbox.isSelected();
        boolean showOrganizerActions = !isAttendingTabActive && !showOnlyPast;

        if (organizerActionsRow != null) {
            organizerActionsRow.setVisible(showOrganizerActions);
            organizerActionsRow.setManaged(showOrganizerActions);
        }
    }

    private Node getNavigationSource() {
        if (rootContainer != null) return rootContainer;
        if (logoutButton != null) return logoutButton;
        if (attendingEventsList != null) return attendingEventsList;
        return organizingEventsList;
    }
    
    private void loadAttendingEvents(boolean showOnlyPast) {
        try {
            List<Registration> registrations;
            
            if (showOnlyPast) {
                registrations = registrationController.handleViewPastRegistrations();
            } else {
                registrations = registrationController.handleViewUpcomingRegistrations();
            }
            
            // Convert registrations to events for display
            List<Event> events = registrations.stream()
                .map(reg -> {
                    try {
                        return eventController.handleGetEventDetails(reg.getEventId());
                    } catch (Exception e) {
                        System.err.println("❌ Error loading event from registration: " + e.getMessage());
                        return null;
                    }
                })
                .filter(event -> event != null)
                .collect(Collectors.toList());
            
            attendingEvents = FXCollections.observableArrayList(events);
            
            if (attendingEventsList != null) {
                attendingEventsList.setItems(attendingEvents);
            }
            
            // Show/hide empty state
            if (attendingEmptyState != null) {
                boolean isEmpty = events.isEmpty();
                attendingEmptyState.setVisible(isEmpty);
                attendingEmptyState.setManaged(isEmpty);
            }
            
            System.out.println("✅ Loaded " + events.size() + " attending events");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading attending events: " + e.getMessage());
            e.printStackTrace();
            if (attendingEmptyState != null) {
                attendingEmptyState.setVisible(true);
                attendingEmptyState.setManaged(true);
            }
        }
    }
    
    private void loadOrganizingEvents(boolean showOnlyPast) {
        try {
            List<Event> events = eventController.handleViewMyEvents();
            
            // Filter based on past events checkbox
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (showOnlyPast) {
                events = events.stream()
                    .filter(e -> e.getEndDateTime() != null && e.getEndDateTime().isBefore(now))
                    .collect(Collectors.toList());
            } else {
                events = events.stream()
                    .filter(e -> e.getEndDateTime() == null || e.getEndDateTime().isAfter(now))
                    .collect(Collectors.toList());
            }
            
            organizingEvents = FXCollections.observableArrayList(events);
            
            if (organizingEventsList != null) {
                organizingEventsList.setItems(organizingEvents);
            }
            
            // Show/hide empty state
            if (organizingEmptyState != null) {
                boolean isEmpty = events.isEmpty();
                organizingEmptyState.setVisible(isEmpty);
                organizingEmptyState.setManaged(isEmpty);
            }
            
            System.out.println("✅ Loaded " + events.size() + " organizing events");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading organizing events: " + e.getMessage());
            e.printStackTrace();
            if (organizingEmptyState != null) {
                organizingEmptyState.setVisible(true);
                organizingEmptyState.setManaged(true);
            }
        }
    }
    
    // ==================== FILTER HANDLER ====================
    
    @FXML
    private void handleTogglePastEvents() {
        boolean showPast = pastEventsCheckbox != null && pastEventsCheckbox.isSelected();
        System.out.println((showPast ? "Showing" : "Hiding") + " past events");
        loadEvents();
    }
    
    // ==================== ACTION HANDLERS ====================
    
    @FXML
    private void handleCancelRegistration() {
        Event selectedEvent = isAttendingTabActive ? 
            (attendingEventsList != null ? attendingEventsList.getSelectionModel().getSelectedItem() : null) : null;
        
        if (selectedEvent == null) {
            showAlert("No Selection", "Please select an event to cancel registration");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Registration");
        confirm.setHeaderText("Cancel registration for: " + selectedEvent.getTitle());
        confirm.setContentText("Are you sure you want to cancel your registration?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean cancelled = registrationController.handleCancelRegistrationForEvent(selectedEvent.getEventId());
                    if (cancelled) {
                        showAlert("Success", "Registration cancelled successfully!");
                        loadEvents(); // Refresh
                    } else {
                        showAlert("Error", "Failed to cancel registration");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error cancelling registration: " + e.getMessage());
                    showAlert("Error", "Failed to cancel registration: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleEditEvent() {
        Event selectedEvent = !isAttendingTabActive ? 
            (organizingEventsList != null ? organizingEventsList.getSelectionModel().getSelectedItem() : null) : null;
        
        if (selectedEvent == null) {
            showAlert("No Selection", "Please select an event to edit");
            return;
        }
        
        // Check if event can be edited
        if (!selectedEvent.canBeUpdated()) {
            showAlert("Cannot Edit", "This event cannot be edited because it has already started or been completed.");
            return;
        }

        NavigationUtil.<CreateEventController>navigateToWithData(
            "/fxml/create-event.fxml",
            "Connect - Edit Event",
            organizingEventsList,
            1200,
            800,
            controller -> {
                if (controller != null) controller.receiveData(selectedEvent.getEventId());
            }
        );
    }
    
    @FXML
    private void handleManageEvent() {
        handleEditEvent();
    }

    @FXML
    private void handleCancelMyEvent() {
        Event selectedEvent = !isAttendingTabActive ?
            (organizingEventsList != null ? organizingEventsList.getSelectionModel().getSelectedItem() : null) : null;

        if (selectedEvent == null) {
            showAlert("No Selection", "Please select an organizing event to cancel");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Event");
        confirm.setHeaderText("Cancel event: " + selectedEvent.getTitle());
        confirm.setContentText("This marks the event as cancelled. Continue?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean cancelled = eventController.handleCancelEvent(selectedEvent.getEventId(), "Cancelled by organizer");
                if (cancelled) {
                    showAlert("Success", "Event cancelled successfully");
                    loadEvents();
                } else {
                    showAlert("Error", "Failed to cancel event");
                }
            }
        });
    }

    @FXML
    private void handleDeleteMyEvent() {
        Event selectedEvent = !isAttendingTabActive ?
            (organizingEventsList != null ? organizingEventsList.getSelectionModel().getSelectedItem() : null) : null;

        if (selectedEvent == null) {
            showAlert("No Selection", "Please select an organizing event to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete event: " + selectedEvent.getTitle());
        confirm.setContentText("This permanently deletes the event (only allowed if no active registrations). Continue?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = eventController.handleDeleteEvent(selectedEvent.getEventId());
                if (deleted) {
                    showAlert("Success", "Event deleted successfully");
                    loadEvents();
                } else {
                    showAlert("Error", "Failed to delete event");
                }
            }
        });
    }
    
    @FXML
    private void handleViewEvent() {
        Event selectedEvent = isAttendingTabActive ? 
            (attendingEventsList != null ? attendingEventsList.getSelectionModel().getSelectedItem() : null) :
            (organizingEventsList != null ? organizingEventsList.getSelectionModel().getSelectedItem() : null);
        
        if (selectedEvent != null) {
            System.out.println("🎯 Navigating to event details for: " + selectedEvent.getTitle());
            System.out.println("🎯 Event ID: " + selectedEvent.getEventId());
            
            NavigationUtil.<EventDetailsController>navigateToWithData(
                "/fxml/event-details.fxml", 
                "Connect - Event Details", 
                logoutButton != null ? logoutButton : attendingEventsList, 
                1200, 
                800,
                controller -> {
                    controller.setEventId(selectedEvent.getEventId());
                }
            );
        } else {
            showAlert("No Selection", "Please select an event to view");
        }
    }
    
    // ==================== NAVIGATION HANDLERS ====================
    
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        NavigationUtil.navigateTo("/fxml/login.fxml", "Connect - Sign In", logoutButton, 400, 600);
    }
    
    @FXML
    private void handleProfile() {
        NavigationUtil.navigateTo("/fxml/profile.fxml", "Connect - Profile", profileButton, 1200, 800);
    }
    
    @FXML
    private void handleBrowseEvents() {
        NavigationUtil.navigateTo("/fxml/browse-events.fxml", "Connect - Browse Events", getNavigationSource(), 1200, 800);
    }
    
    @FXML
    private void handleCertificates() {
        NavigationUtil.navigateTo("/fxml/certificates.fxml", "Connect - Certificates", getNavigationSource(), 1200, 800);
    }
    
    @FXML
    private void handleCreateEvent() {
        NavigationUtil.navigateTo("/fxml/create-event.fxml", "Connect - Create Event", getNavigationSource(), 1200, 800);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Custom ListCell for displaying events
     */
    private class EventListCell extends ListCell<Event> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        
        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);
            
            if (empty || event == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox container = new VBox(5);
                
                // Header with status and category
                HBox header = new HBox(5);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                Label status = new Label(event.getEventStatus().toString());
                status.setStyle(getStatusStyle(event.getEventStatus()));
                
                Label category = new Label(event.getCategory());
                category.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #0678FC; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                
                header.getChildren().addAll(status, category);
                
                Label title = new Label(event.getTitle());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label details = new Label(
                    "📅 " + event.getStartDateTime().format(formatter) + 
                    " | 📍 " + event.getVenue() + 
                    " | 👥 " + event.getCurrentRegistrations() + "/" + event.getCapacity()
                );
                details.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                
                container.getChildren().addAll(header, title, details);
                setGraphic(container);
            }
        }
        
        private String getStatusStyle(com.connect.enums.EventStatus status) {
            String baseStyle = "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;";
            
            switch (status) {
                case APPROVED:
                    return baseStyle + " -fx-background-color: #33CE9A; -fx-text-fill: white;";
                case ONGOING:
                    return baseStyle + " -fx-background-color: #0678FC; -fx-text-fill: white;";
                case COMPLETED:
                    return baseStyle + " -fx-background-color: #6C757D; -fx-text-fill: white;";
                case CANCELLED:
                    return baseStyle + " -fx-background-color: #DC3545; -fx-text-fill: white;";
                case PENDING_APPROVAL:
                    return baseStyle + " -fx-background-color: #FFC107; -fx-text-fill: black;";
                default:
                    return baseStyle + " -fx-background-color: #6C757D; -fx-text-fill: white;";
            }
        }
    }
}
