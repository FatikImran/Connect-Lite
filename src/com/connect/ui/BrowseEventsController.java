package com.connect.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.connect.controller.EventController;
import com.connect.controller.RegistrationController;
import com.connect.enums.EventStatus;
import com.connect.model.Event;
import com.connect.util.NavigationUtil;
import com.connect.ui.EventDetailsController;
import com.connect.util.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BrowseEventsController {
    
    // Navigation buttons (from your UI design)
    @FXML private Button logoutButton;
    @FXML private Button myEventsButton;
    
    // Search and filters (from your UI design)
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> dateFilter;
    @FXML private ComboBox<String> statusFilter;
    
    // Containers (from your UI design)
    @FXML private VBox trendingSection;
    @FXML private VBox trendingEventsContainer;
    @FXML private GridPane eventsGrid;
    @FXML private Label eventCountLabel;
    @FXML private VBox emptyState;
    @FXML private VBox loadingIndicator;
    
    // Additional fields for backend integration
    @FXML private Label welcomeLabel;
    @FXML private Button refreshButton;
    
    // Backend controllers
    private final EventController eventController = new EventController();
    private final RegistrationController registrationController = new RegistrationController();
    
    private ObservableList<Event> eventsList;
    
    @FXML
    public void initialize() {
        System.out.println("✅ BrowseEventsController initialized!");
        
        // Initialize ComboBoxes programmatically
        initializeComboBoxes();
        
        // Set initial event count
        if (eventCountLabel != null) {
            eventCountLabel.setText("(0 events)");
        }
        
        // Display welcome message
        displayWelcomeMessage();
        
        // Load events
        loadEvents();
        
        System.out.println("🎯 All components initialized successfully!");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔁 Refreshing events list...");
        try {
            // Re-apply current filters/search to refresh results
            filterEvents();
        } catch (Exception e) {
            System.err.println("❌ Refresh failed: " + e.getMessage());
            loadEvents();
        }
    }
    
    private void initializeComboBoxes() {
        System.out.println("🔄 Initializing ComboBox filters...");
        
        // Initialize category filter
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll(
                "All Categories", 
                "Technology", 
                "Sports", 
                "Arts & Culture", 
                "Education", 
                "Health & Wellness", 
                "Business", 
                "Entertainment", 
                "Social", 
                "Other"
            );
            categoryFilter.setValue("All Categories");
            System.out.println("✅ Category filter initialized with " + categoryFilter.getItems().size() + " items");
        }
        
        // Initialize date filter
        if (dateFilter != null) {
            dateFilter.getItems().addAll(
                "All Dates", 
                "Today", 
                "This Week", 
                "This Month", 
                "Upcoming",
                "Past Events"
            );
            dateFilter.setValue("All Dates");
            System.out.println("✅ Date filter initialized with " + dateFilter.getItems().size() + " items");
        }
        
        // Initialize status filter
        if (statusFilter != null) {
            statusFilter.getItems().addAll(
                "All Events", 
                "Open for Registration", 
                "Full", 
                "Ongoing", 
                "Completed",
                "Upcoming"
            );
            statusFilter.setValue("All Events");
            System.out.println("✅ Status filter initialized with " + statusFilter.getItems().size() + " items");
        }
    }
    
    private void displayWelcomeMessage() {
        if (welcomeLabel != null) {
            String userName = SessionManager.getCurrentUserName();
            welcomeLabel.setText("Welcome, " + (userName != null ? userName : "User") + "!");
        }
    }
    
    // ==================== SEARCH & FILTER HANDLERS ====================
    
    @FXML
    private void handleSearch() {
        String searchText = searchField != null ? searchField.getText().trim() : "";
        System.out.println("🔍 Searching for: " + searchText);
        
        // TODO: Implement search logic with database
        filterEvents();
    }
    
    @FXML
    private void handleFilterChange() {
        String category = categoryFilter != null ? categoryFilter.getValue() : "All Categories";
        String date = dateFilter != null ? dateFilter.getValue() : "All Dates";
        String status = statusFilter != null ? statusFilter.getValue() : "All Events";
        
        System.out.println("🔧 Filters changed - Category: " + category + ", Date: " + date + ", Status: " + status);
        
        // TODO: Implement filter logic with database
        filterEvents();
    }
    
    @FXML
    private void handleClearFilters() {
        System.out.println("🔄 Clearing all filters...");
        
        if (searchField != null) searchField.clear();
        if (categoryFilter != null) categoryFilter.setValue("All Categories");
        if (dateFilter != null) dateFilter.setValue("All Dates");
        if (statusFilter != null) statusFilter.setValue("All Events");
        
        loadEvents();
    }
    
    // ==================== DATA LOADING ====================
    
    private void loadEvents() {
        System.out.println("📊 Loading events from database...");
        
        // Show loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
        }
        
        try {
            List<Event> events = eventController.handleBrowseEvents();
            eventsList = FXCollections.observableArrayList(events);
            
            // Update count
            if (eventCountLabel != null) {
                eventCountLabel.setText("(" + events.size() + " events)");
            }
            
            // Show empty state if no events
            if (events.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                populateEventsGrid();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading events: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        } finally {
            // Hide loading indicator
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            }
        }
    }

    private void populateEventsGrid() {
        if (eventsGrid == null) return;

        eventsGrid.getChildren().clear();

        int cols = 3;
        int row = 0;
        int col = 0;

        for (Event event : eventsList) {
            VBox card = createEventCard(event);

            eventsGrid.add(card, col, row);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        card.setPrefWidth(350);

        // Status badge
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label statusLabel = new Label(event.getEventStatus().toString());
        statusLabel.setStyle(getStatusBadgeStyle(event.getEventStatus()));
        
        Label categoryLabel = new Label(event.getCategory());
        categoryLabel.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #0678FC; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(statusLabel, categoryLabel);

        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #122438;");
        title.setWrapText(true);

        Label when = new Label(event.getStartDateTime() != null ? 
            event.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) : "Date TBD");
        when.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");

        Label where = new Label(event.getVenue() != null ? event.getVenue() : "Venue TBD");
        where.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");

        Label participants = new Label(event.getCurrentRegistrations() + "/" + event.getCapacity());
        participants.setStyle("-fx-text-fill: #0678FC; -fx-font-weight: bold; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        actions.setStyle("-fx-alignment: center-right;");

        Button viewBtn = new Button("View Details");
        viewBtn.setStyle("-fx-background-color: #0678FC; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 12;");

        // Navigate to event details with the specific eventId
        viewBtn.setOnAction(e -> {
            NavigationUtil.<EventDetailsController>navigateToWithData(
                "/fxml/event-details.fxml",
                "Connect - Event Details",
                viewBtn,
                1200,
                800,
                controller -> {
                    if (controller != null) controller.setEventId(event.getEventId());
                }
            );
        });

        actions.getChildren().addAll(viewBtn);
        card.getChildren().addAll(headerBox, title, when, where, participants, actions);
        return card;
    }
    
    private String getStatusBadgeStyle(com.connect.enums.EventStatus status) {
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
    
    private void filterEvents() {
        System.out.println("🔍 Filtering events...");
        String searchText = searchField != null ? searchField.getText().trim() : "";
        String category = (categoryFilter != null) ? categoryFilter.getValue() : "All Categories";
        String date = (dateFilter != null) ? dateFilter.getValue() : "All Dates";
        String status = (statusFilter != null) ? statusFilter.getValue() : "All Events";

        // Normalize parameters for controller/service
        String searchQuery = searchText.isEmpty() ? null : searchText;
        String categoryParam = (category == null || "All Categories".equals(category)) ? null : category;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        LocalDateTime now = LocalDateTime.now();

        if ("Today".equals(date)) {
            startDate = now.toLocalDate().atStartOfDay();
            endDate = startDate.plusDays(1).minusNanos(1);
        } else if ("This Week".equals(date)) {
            startDate = now.toLocalDate().atStartOfDay();
            endDate = startDate.plusDays(7).minusNanos(1);
        } else if ("This Month".equals(date)) {
            startDate = now.toLocalDate().atStartOfDay();
            endDate = startDate.plusMonths(1).minusNanos(1);
        } else if ("Upcoming".equals(date)) {
            startDate = now;
            endDate = null;
        } else if ("Past Events".equals(date)) {
            startDate = null;
            endDate = now;
        }

        try {
            List<Event> results = eventController.handleSearchEvents(searchQuery, categoryParam, startDate, endDate);

            // Apply status filter client-side
            if (results != null && !results.isEmpty() && status != null && !"All Events".equals(status)) {
                results = results.stream()
                    .filter(event -> {
                        switch (status) {
                            case "Open for Registration":
                                return event.isRegistrationOpen() && event.hasCapacity();
                            case "Full":
                                return !event.hasCapacity();
                            case "Ongoing":
                                return event.getEventStatus() == com.connect.enums.EventStatus.ONGOING;
                            case "Completed":
                                return event.getEventStatus() == com.connect.enums.EventStatus.COMPLETED;
                            case "Upcoming":
                                return event.getEventStatus() == com.connect.enums.EventStatus.APPROVED && 
                                       event.getStartDateTime().isAfter(now);
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());
            }

            eventsList = FXCollections.observableArrayList(results);

            if (eventCountLabel != null) eventCountLabel.setText("(" + eventsList.size() + " events)");

            if (eventsList.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                populateEventsGrid();
            }

        } catch (Exception e) {
            System.err.println("❌ Error filtering events: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter events: " + e.getMessage());
            // fallback to full load
            loadEvents();
        }
    }
    
    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
        }
        // Clear any previously shown event cards so stale data is not visible
        try {
            if (eventsGrid != null) eventsGrid.getChildren().clear();
        }
        finally {
			if (eventCountLabel != null) eventCountLabel.setText("(0 events)");
		}
    }
    
    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
        }
    }
    
    // ==================== NAVIGATION HANDLERS ====================
    
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Logging out...");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be redirected to the login screen.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                NavigationUtil.navigateTo("/fxml/login.fxml", "Connect - Sign In", logoutButton, 400, 600);
            }
        });
    }

    @FXML
    private void handleMyEvents() {
        System.out.println("📅 Navigating to my registrations...");
        NavigationUtil.navigateTo("/fxml/my-registrations.fxml", "Connect - My Registrations", myEventsButton, 1200, 800);
    }

    // ==================== UTILITY METHODS ====================
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
