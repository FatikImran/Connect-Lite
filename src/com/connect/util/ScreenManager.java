package com.connect.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {
    
    // Screen configurations - Add new screens here only
    public enum Screen {
        // Authentication screens
        LOGIN("/fxml/login.fxml", "Connect - Sign In", 400, 600),
        SIGNUP("/fxml/signup.fxml", "Connect - Sign Up", 400, 650),

        // Main application screens
        BROWSE_EVENTS("/fxml/browse-events.fxml", "Connect - Browse Events", 1200, 800),
        EVENT_DETAILS("/fxml/event-details.fxml", "Connect - Event Details", 1200, 800),
        MY_REGISTRATIONS("/fxml/my-registrations.fxml", "Connect - My Registrations", 1200, 800);
        private final String fxmlPath;
        private final String title;
        private final int width;
        private final int height;
        
        Screen(String fxmlPath, String title, int width, int height) {
            this.fxmlPath = fxmlPath;
            this.title = title;
            this.width = width;
            this.height = height;
        }
        
        public String getFxmlPath() { return fxmlPath; }
        public String getTitle() { return title; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
    
    private static final Map<String, Object> controllers = new HashMap<>();
    
    /**
     * Navigate to a screen without passing data
     */
    public static void navigateTo(Screen screen, Node currentNode) {
        navigate(screen, currentNode, null);
    }
    
    /**
     * Navigate to a screen with data
     */
    public static void navigateTo(Screen screen, Node currentNode, Object data) {
        navigate(screen, currentNode, data);
    }
    
    /**
     * Open a new window (for popups/modals)
     */
    public static void openWindow(Screen screen, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource(screen.getFxmlPath()));
            Parent root = loader.load();
            
            // Pass data to controller if supported
            Object controller = loader.getController();
            if (data != null && controller instanceof DataReceiver) {
                ((DataReceiver) controller).receiveData(data);
            }
            
            controllers.put(screen.getFxmlPath(), controller);
            
            Stage newStage = new Stage();
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            scene.getStylesheets().add(ScreenManager.class.getResource("/css/connect-theme.css").toExternalForm());
            
            newStage.setTitle(screen.getTitle());
            newStage.setScene(scene);
            newStage.setResizable(screen.getWidth() > 400); // Only main screens are resizable
            newStage.show();
            
        } catch (IOException e) {
            handleError("Navigation Error", "Unable to open: " + screen.getFxmlPath(), e);
        }
    }
    
    /**
     * Core navigation method
     */
    private static void navigate(Screen screen, Node currentNode, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource(screen.getFxmlPath()));
            Parent root = loader.load();
            
            // Pass data to controller if supported
            Object controller = loader.getController();
            if (data != null && controller instanceof DataReceiver) {
                ((DataReceiver) controller).receiveData(data);
            }
            
            controllers.put(screen.getFxmlPath(), controller);
            
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            scene.getStylesheets().add(ScreenManager.class.getResource("/css/connect-theme.css").toExternalForm());
            
            stage.setTitle(screen.getTitle());
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            handleError("Navigation Error", "Unable to load: " + screen.getFxmlPath(), e);
        }
    }
    
    /**
     * Get controller for a specific screen
     */
    @SuppressWarnings("unchecked")
    public static <T> T getController(Screen screen) {
        return (T) controllers.get(screen.getFxmlPath());
    }
    
    /**
     * Navigate back to previous screen (simple implementation)
     */
    public static void navigateBack(Node currentNode) {
        // For now, navigate to browse events as default back location
        // You can enhance this with a navigation stack later
        navigateTo(Screen.BROWSE_EVENTS, currentNode);
    }
    
    /**
     * Navigate to appropriate dashboard based on user type
     */
    public static void navigateToDashboard(Node currentNode) {
        navigateTo(Screen.BROWSE_EVENTS, currentNode);
    }
    
    /**
     * Error handling
     */
    private static void handleError(String title, String message, Exception e) {
        System.err.println(title + ": " + message);
        e.printStackTrace();
        
        // You can also show an alert dialog here if needed
        // showAlert(Alert.AlertType.ERROR, title, message);
    }
}