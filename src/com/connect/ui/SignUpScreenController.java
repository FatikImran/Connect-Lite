package com.connect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import com.connect.util.DatabaseConnection;
import com.connect.util.NavigationUtil;
import com.connect.util.ValidationUtil;
import com.connect.controller.AuthController;
import com.connect.model.User;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SignUpScreenController {
    
    // Profile picture
    @FXML private ImageView profileImageView;
    @FXML private Button uploadImageButton;
    
    // Form fields
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextArea bioField;
    
    // Error labels
    @FXML private Label fullNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    
    // Buttons
    @FXML private Button signUpButton;
    @FXML private Hyperlink signInLink;
    
    // Backend controller
    private final AuthController authController = new AuthController();
    
    private File selectedImageFile = null;
    
    @FXML
    public void initialize() {
        System.out.println("✅ SignUpScreenController initialized!");
        
        signUpButton.setOnAction(e -> handleSignUp());
        signInLink.setOnAction(e -> handleNavigateToSignIn());
        uploadImageButton.setOnAction(e -> handleUploadImage());
        
        // Hide all error labels initially
        hideAllErrors();
        
        System.out.println("📋 FXML Fields Status:");
        System.out.println("  - fullNameField: " + (fullNameField != null ? "✅" : "❌"));
        System.out.println("  - emailField: " + (emailField != null ? "✅" : "❌"));
        System.out.println("  - phoneField: " + (phoneField != null ? "✅" : "❌ (optional)"));
        System.out.println("  - passwordField: " + (passwordField != null ? "✅" : "❌"));
        System.out.println("  - confirmPasswordField: " + (confirmPasswordField != null ? "✅" : "❌"));
    }
    
    private boolean checkDatabaseBeforeSignUp() {
        System.out.println("🔍 Checking database connection before sign up...");
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection verified!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            
            // Show user-friendly error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Failed");
            alert.setHeaderText("Cannot connect to database");
            alert.setContentText(
                "Please ask your friend to:\n\n" +
                "1. Start XAMPP Control Panel\n" +
                "2. Click 'Start' next to MySQL\n" +
                "3. Ensure MySQL service is running (green indicator)\n\n" +
                "Error: " + e.getMessage()
            );
            alert.showAndWait();
        }
        return false;
    }
    // ==================== IMAGE UPLOAD ====================
    
    @FXML
    private void handleUploadImage() {
        System.out.println("📸 Opening file chooser for profile picture...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        
        if (selectedImageFile != null) {
            System.out.println("✅ Image selected: " + selectedImageFile.getName());
            // TODO: Load and display image in profileImageView
            // TODO: Store image path for database
        }
    }
    
    // ==================== FORM VALIDATION ====================
    
    @FXML
    private void handleSignUp() {
        System.out.println("📝 Attempting sign up...");
        
        // 	Check database first
        if (!checkDatabaseBeforeSignUp()) {
            return;
        }
        
        if (validateForm()) {
            try {
                // Get form data
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField != null ? phoneField.getText().trim() : "";
                String password = passwordField.getText();
                
                // Disable button to prevent multiple submissions
                signUpButton.setDisable(true);
                
                // Register via backend
                User user = authController.handleSignUp(fullName, email, phone, password, false);
                
                if (user != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Account created successfully! Welcome to Connect, " + user.getName() + "!");
                    
                    // Navigate to browse events (user is auto-logged in)
                    NavigationUtil.navigateTo("/fxml/browse-events.fxml", "Connect - Browse Events", signUpButton, 1200, 800);
                } else {
                    showError("Registration failed. Email may already be in use.");
                    signUpButton.setDisable(false);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Sign up error: " + e.getMessage());
                e.printStackTrace();
                showError("Registration failed: " + e.getMessage());
                signUpButton.setDisable(false);
            }
        }
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        hideAllErrors();
        
        // Validate full name
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showError(fullNameError, "Full name is required");
            isValid = false;
        } else {
            String nameError = ValidationUtil.getNameError(fullName);
            if (nameError != null) {
                showError(fullNameError, nameError);
                isValid = false;
            }
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailError, "Email is required");
            isValid = false;
        } else {
            String emailErrorMsg = ValidationUtil.getEmailError(email);
            if (emailErrorMsg != null) {
                showError(emailError, emailErrorMsg);
                isValid = false;
            }
        }
        
        // Validate phone (optional but must be valid if provided)
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        if (!phone.isEmpty()) {
            String phoneErrorMsg = ValidationUtil.getPhoneError(phone);
            if (phoneErrorMsg != null) {
                showError(phoneError, phoneErrorMsg);
                isValid = false;
            }
        }
        
        // Validate password
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showError(passwordError, "Password is required");
            isValid = false;
        } else {
            String passwordErrorMsg = ValidationUtil.getPasswordError(password);
            if (passwordErrorMsg != null) {
                showError(passwordError, passwordErrorMsg);
                isValid = false;
            }
        }
        
        // Validate confirm password
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            showError(confirmPasswordError, "Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(confirmPasswordError, "Passwords do not match");
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidPhone(String phone) {
        // Pakistan phone format: 03XX-XXXXXXX or 03XXXXXXXXX
        return phone.matches("^03\\d{2}-?\\d{7}$");
    }
    
    private void hideAllErrors() {
        if (fullNameError != null) fullNameError.setVisible(false);
        if (emailError != null) emailError.setVisible(false);
        if (phoneError != null) phoneError.setVisible(false);
        if (passwordError != null) passwordError.setVisible(false);
        if (confirmPasswordError != null) confirmPasswordError.setVisible(false);
    }
    
    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
    
    // ==================== NAVIGATION ====================
    
    @FXML
    private void handleNavigateToSignIn() {
        NavigationUtil.navigateTo("/fxml/login.fxml", "Connect - Sign In", signInLink, 400, 600);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        // Show in a general error area or alert
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}