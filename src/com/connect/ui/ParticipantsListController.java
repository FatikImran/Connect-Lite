package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.connect.util.DataReceiver;
import com.connect.controller.RegistrationController;
import com.connect.controller.UserController;
import com.connect.model.User;

import java.util.List;

public class ParticipantsListController implements DataReceiver {

    @FXML private ListView<String> participantsList;
    @FXML private Button closeBtn;

    private final RegistrationController registrationController = new RegistrationController();
    private final UserController userController = new UserController();

    private String eventId;

    @FXML
    public void initialize() {
        if (closeBtn != null) closeBtn.setOnAction(e -> closeBtn.getScene().getWindow().hide());
    }

    @Override
    public void receiveData(Object data) {
        if (data == null) return;
        if (data instanceof String) {
            this.eventId = (String) data;
            loadParticipants();
        }
    }

    private void loadParticipants() {
        if (eventId == null || eventId.isEmpty()) return;

        try {
            List<com.connect.model.Registration> regs = registrationController.handleGetPublicEventRegistrations(eventId);
            participantsList.getItems().clear();

            for (com.connect.model.Registration r : regs) {
                String pid = r.getParticipantId();
                String name = pid;
                try {
                    User u = userController.handleGetUserProfile(pid);
                    if (u != null && u.getName() != null && !u.getName().isEmpty()) name = u.getName();
                } catch (Exception ex) {
                    // ignore, fallback to id
                }
                String line = name + " (" + r.getStatus().name() + ")";
                participantsList.getItems().add(line);
            }

            if (regs.isEmpty()) participantsList.getItems().add("No participants yet");

        } catch (Exception e) {
            participantsList.getItems().clear();
            participantsList.getItems().add("Failed to load participants: " + e.getMessage());
        }
    }
}
