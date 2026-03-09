package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import com.connect.controller.EventController;
import com.connect.model.Event;
import com.connect.util.DataReceiver;
import com.connect.util.NavigationUtil;
import com.connect.util.SessionManager;
import com.connect.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CreateEventController implements DataReceiver {

    @FXML private VBox rootContainer;

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField eventTitleField;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private DatePicker registrationDeadlinePicker;
    @FXML private TextField deadlineTimeField;
    @FXML private TextField venueField;
    @FXML private TextField capacityField;
    @FXML private TextArea descriptionArea;
    @FXML private Button createEventButton;

    private final EventController eventController = new EventController();

    private String editingEventId;
    private boolean isEditMode = false;
    private String pendingEditEventId;

    @FXML
    public void initialize() {
        initializeCategoryComboBox();
        setupFormValidation();

        if (startTimeField != null) startTimeField.setPromptText("HH:MM (e.g., 14:30)");
        if (endTimeField != null) endTimeField.setPromptText("HH:MM (e.g., 16:30)");
        if (deadlineTimeField != null) deadlineTimeField.setPromptText("HH:MM (e.g., 12:00)");

        if (pendingEditEventId != null && !pendingEditEventId.isBlank()) {
            String eventId = pendingEditEventId;
            pendingEditEventId = null;
            loadEventForEdit(eventId);
        }
    }

    @Override
    public void receiveData(Object data) {
        if (data == null) return;
        if (data instanceof String) {
            String eventId = (String) data;
            if (!eventId.isBlank()) {
                // When called via NavigationUtil controller factory, this may run
                // before FXML field injection. Defer until initialize() in that case.
                if (eventTitleField == null) {
                    pendingEditEventId = eventId;
                } else {
                    loadEventForEdit(eventId);
                }
            }
        }
    }

    private void initializeCategoryComboBox() {
        if (categoryCombo != null) {
            categoryCombo.getItems().addAll(
                "Technology", "Sports", "Arts & Culture", "Education",
                "Health & Wellness", "Business", "Entertainment", "Social", "Other"
            );
        }
    }

    private void setupFormValidation() {
        if (capacityField != null) {
            capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    capacityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    private void loadEventForEdit(String eventId) {
        try {
            Event existing = eventController.handleGetEventDetails(eventId);
            if (existing == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load event for editing.");
                return;
            }

            this.isEditMode = true;
            this.editingEventId = eventId;

            eventTitleField.setText(existing.getTitle());
            descriptionArea.setText(existing.getDescription());
            venueField.setText(existing.getVenue());
            categoryCombo.setValue(existing.getCategory());
            capacityField.setText(String.valueOf(existing.getCapacity()));

            startDatePicker.setValue(existing.getStartDateTime().toLocalDate());
            startTimeField.setText(toTimeString(existing.getStartDateTime().toLocalTime()));

            endDatePicker.setValue(existing.getEndDateTime().toLocalDate());
            endTimeField.setText(toTimeString(existing.getEndDateTime().toLocalTime()));

            registrationDeadlinePicker.setValue(existing.getRegistrationDeadline().toLocalDate());
            deadlineTimeField.setText(toTimeString(existing.getRegistrationDeadline().toLocalTime()));

            if (createEventButton != null) {
                createEventButton.setText("UPDATE EVENT");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load event: " + e.getMessage());
        }
    }

    private String toTimeString(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    @FXML
    private void handleCreateEvent() {
        if (!validateForm()) {
            return;
        }

        try {
            String title = eventTitleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String venue = venueField.getText().trim();
            String category = categoryCombo.getValue();

            LocalDateTime startDateTime = parseDateTime(startDatePicker, startTimeField, "Start");
            if (startDateTime == null) return;

            LocalDateTime endDateTime = parseDateTime(endDatePicker, endTimeField, "End");
            if (endDateTime == null) return;

            LocalDateTime registrationDeadline = parseDateTime(
                registrationDeadlinePicker, deadlineTimeField, "Registration deadline"
            );
            if (registrationDeadline == null) return;

            if (!ValidationUtil.isValidDateRange(startDateTime, endDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "End date/time must be after start date/time");
                return;
            }

            if (!ValidationUtil.isInFuture(startDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Event start time must be in the future");
                return;
            }

            if (registrationDeadline.isAfter(startDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Registration deadline must be before event start time");
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
                if (!ValidationUtil.isValidCapacity(capacity)) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be between 1 and 10,000");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid capacity. Please enter a number.");
                return;
            }

            if (isEditMode) {
                boolean updated = eventController.handleUpdateEvent(
                    editingEventId,
                    title,
                    description,
                    startDateTime,
                    endDateTime,
                    venue,
                    category,
                    capacity,
                    registrationDeadline
                );

                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!");
                    handleMyEvents();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event.");
                }
            } else {
                Event event = eventController.handleCreateEvent(
                    title,
                    description,
                    startDateTime,
                    endDateTime,
                    venue,
                    category,
                    capacity,
                    registrationDeadline
                );

                if (event != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event created successfully and submitted for review.");
                    handleMyEvents();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event.");
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Operation failed: " + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(DatePicker datePicker, TextField timeField, String fieldName) {
        if (datePicker == null || datePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " date is required");
            return null;
        }

        LocalDate date = datePicker.getValue();
        LocalTime time;

        try {
            if (timeField == null || timeField.getText().trim().isEmpty()) {
                time = LocalTime.of(0, 0);
            } else {
                String[] parts = timeField.getText().trim().split(":");
                if (parts.length != 2) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " time format invalid. Use HH:MM");
                    return null;
                }

                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " time invalid. Hours: 0-23, Minutes: 0-59");
                    return null;
                }

                time = LocalTime.of(hour, minute);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " time format invalid. Use HH:MM");
            return null;
        }

        return LocalDateTime.of(date, time);
    }

    private boolean validateForm() {
        if (eventTitleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Event title is required.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a category.");
            return false;
        }

        if (capacityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity is required.");
            return false;
        }

        if (venueField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Venue is required.");
            return false;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null || registrationDeadlinePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Start, end, and registration deadline dates are required.");
            return false;
        }

        return true;
    }

    @FXML
    private void handleSaveAsDraft() {
        showAlert(Alert.AlertType.INFORMATION, "Not Implemented", "Draft saving is not in Sprint 2 scope.");
    }

    @FXML
    private void handleBack() {
        handleMyEvents();
    }

    @FXML
    private void handleBrowseEvents() {
        NavigationUtil.navigateTo("/fxml/browse-events.fxml", "Connect - Browse Events", rootContainer, 1200, 800);
    }

    @FXML
    private void handleMyEvents() {
        NavigationUtil.navigateTo("/fxml/my-registrations.fxml", "Connect - My Registrations", rootContainer, 1200, 800);
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        NavigationUtil.navigateTo("/fxml/login.fxml", "Connect - Sign In", rootContainer, 400, 600);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
