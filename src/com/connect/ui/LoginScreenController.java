package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.connect.util.NavigationUtil;
import com.connect.util.SessionManager;
import com.connect.controller.AuthController;
import com.connect.model.User;

public class LoginScreenController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Hyperlink signUpLink;
    @FXML private Button signInButton;
    @FXML private Label errorLabel;
    
    // Backend controller
    private final AuthController authController = new AuthController();
    
    @FXML
    public void initialize() {
        System.out.println("✅ LoginScreenController initialized!");
        setupEventHandlers();
        loadRememberedEmail();
    }
    
    private void setupEventHandlers() {
        signInButton.setOnAction(e -> handleSignIn());
        signUpLink.setOnAction(e -> handleNavigateToSignUp());

        // Allow Enter key to submit
        emailField.setOnAction(e -> handleSignIn());
        passwordField.setOnAction(e -> handleSignIn());
    }
    
    /**
     * Load remembered email if "Remember Me" was checked previously
     */
    private void loadRememberedEmail() {
        String rememberedEmail = SessionManager.getRememberedEmail();
        if (rememberedEmail != null) {
            emailField.setText(rememberedEmail);
            rememberMeCheckBox.setSelected(true);
        }
    }
    
    @FXML
    private void handleSignIn() {
        // Clear previous error
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Client-side validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
		if (!authController.isValidEmail(email)) {
            showError("Invalid email format");
            return;
        }
        
        // Disable button to prevent multiple clicks
        signInButton.setDisable(true);
        
        try {
            // Authenticate via backend
            User user = authController.handleLogin(email, password);
            
            if (user != null) {
                // Handle "Remember Me"
                if (rememberMeCheckBox.isSelected()) {
                    SessionManager.rememberEmail(email);
                } else {
                    SessionManager.clearRememberedEmail();
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful! Navigating to dashboard...");

                // Navigate to browse events
                NavigationUtil.navigateTo("/fxml/browse-events.fxml", "Connect - Browse Events", signInButton, 1200, 800);
            } else {
                showError("Invalid email or password");
                signInButton.setDisable(false);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            showError("Login failed: " + e.getMessage());
            signInButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleNavigateToSignUp() {
        NavigationUtil.navigateTo("/fxml/signup.fxml", "Connect - Sign Up", signUpLink, 400, 650);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #DC3545;");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", message);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}