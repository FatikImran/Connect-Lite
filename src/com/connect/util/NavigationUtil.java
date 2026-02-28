package com.connect.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import com.connect.util.DataReceiver;
import java.util.function.Consumer;

public class NavigationUtil {
    
    public static void navigateTo(String fxmlFile, String title, Node currentNode, int width, int height) {
        try {
            System.out.println("🔄 Attempting to navigate to: " + fxmlFile);
            
            // Check if the FXML file exists
            java.net.URL resource = NavigationUtil.class.getResource(fxmlFile);
            if (resource == null) {
                // Try classloader lookup without leading slash
                String alt = fxmlFile.startsWith("/") ? fxmlFile.substring(1) : fxmlFile;
                resource = NavigationUtil.class.getClassLoader().getResource(alt);
            }
            if (resource == null) {
                // Try resources/ prefix (some build tools place resources there)
                String pref = fxmlFile.startsWith("/") ? ("resources" + fxmlFile) : ("resources/" + fxmlFile);
                resource = NavigationUtil.class.getClassLoader().getResource(pref);
            }

            if (resource == null) {
                // Final fallback: check a "bin" folder in the working directory (some runtimes copy resources there)
                try {
                    String cwd = System.getProperty("user.dir");
                    String candidate = cwd + (fxmlFile.startsWith("/") ? ("/bin" + fxmlFile) : ("/bin/" + fxmlFile));
                    java.io.File file = new java.io.File(candidate);
                    if (file.exists()) {
                        resource = file.toURI().toURL();
                        System.out.println("🔎 Found FXML in working dir bin: " + candidate);
                    }
                } catch (Exception ex) {
                    // ignore and throw below
                }
                if (resource == null) {
                    throw new IOException("FXML file not found: " + fxmlFile);
                }
            }

            System.out.println("🔎 Using FXML resource: " + resource.toString());

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root;
            try {
                root = loader.load();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Cannot load screen", "Error loading: " + fxmlFile + "\n\n" + ex.toString());
                throw ex;
            }
            
            Stage stage = (currentNode != null && currentNode.getScene() != null) ?
                (Stage) currentNode.getScene().getWindow() : new Stage();

            Scene scene = new Scene(root, width, height);
            
            // Try to load CSS, but don't fail if it doesn't exist
            try {
                String cssPath = NavigationUtil.class.getResource("/css/connect-theme.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
                System.out.println("✅ CSS loaded successfully");
            } catch (Exception e) {
                System.out.println("ℹ️  No CSS file found, using inline styles only");
            }
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("✅ Navigation successful to: " + title);
            
        } catch (IOException e) {
            System.err.println("❌ Navigation Error: " + e.getMessage());
            showErrorAlert("Cannot load screen", 
                "Error loading: " + fxmlFile + "\n\n" +
                "File might be missing or has syntax errors.\n" +
                "Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Navigation Error", 
                "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public static <T> T navigateToWithData(String fxmlPath, String title, Node sourceNode, 
                                          int width, int height, Consumer<T> controllerInitializer) {
        try {
            System.out.println("🔄 Attempting to navigate with data to: " + fxmlPath);
            
            // Check if the FXML file exists
            java.net.URL resource = NavigationUtil.class.getResource(fxmlPath);
            if (resource == null) {
                String alt = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                resource = NavigationUtil.class.getClassLoader().getResource(alt);
            }
            if (resource == null) {
                String pref = fxmlPath.startsWith("/") ? ("resources" + fxmlPath) : ("resources/" + fxmlPath);
                resource = NavigationUtil.class.getClassLoader().getResource(pref);
            }
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            System.out.println("🔎 Using FXML resource: " + resource.toString());

            FXMLLoader loader = new FXMLLoader(resource);

            // If caller provided a controller initializer, install a controller factory that
            // creates the controller instance and applies the initializer BEFORE the
            // controller's initialize() method is invoked by FXMLLoader. This ensures
            // controllers that rely on incoming data during initialization (e.g. EventDetailsController)
            // receive their data in time.
            if (controllerInitializer != null) {
                loader.setControllerFactory(cls -> {
                    try {
                        Object controller = cls.getDeclaredConstructor().newInstance();
                        try {
                            //noinspection unchecked
                            controllerInitializer.accept((T) controller);
                        } catch (ClassCastException ex) {
                            System.out.println("⚠️ Controller initializer type mismatch: " + ex.getMessage());
                        }
                        return controller;
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to create controller instance: " + ex.getMessage(), ex);
                    }
                });
            }

            Parent root;
            try {
                root = loader.load();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Cannot load screen", "Error loading: " + fxmlPath + "\n\n" + ex.toString());
                return null;
            }
            
            // Get controller (it will have been created via factory above if initializer was provided)
            T controller = loader.getController();
            if (controller != null) {
                System.out.println("✅ Controller instance ready: " + controller.getClass().getName());
            } else {
                System.out.println("ℹ️ Controller is null after loading (no fx:controller set in FXML?)");
            }
            
            Stage stage = (sourceNode != null && sourceNode.getScene() != null) ?
                (Stage) sourceNode.getScene().getWindow() : new Stage();
            Scene scene = new Scene(root, width, height);
            
            // Try to load CSS, but don't fail if it doesn't exist
            try {
                String cssPath = NavigationUtil.class.getResource("/css/connect-theme.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
                System.out.println("✅ CSS loaded successfully");
            } catch (Exception e) {
                System.out.println("ℹ️  No CSS file found, using inline styles only");
            }
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("✅ Navigation with data successful to: " + title);
            
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Cannot load screen", 
                "Error loading: " + fxmlPath + "\n\n" +
                "File might be missing or has syntax errors.\n" +
                "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Overload accepting a DataReceiver initializer to avoid compile-time generics
     * coupling when callers only need to pass data to controllers implementing
     * the DataReceiver interface.
     */
    public static DataReceiver navigateToWithDataReceiver(String fxmlPath, String title, Node sourceNode,
                                                 int width, int height, Consumer<DataReceiver> controllerInitializer) {
        try {
            System.out.println("🔄 Attempting to navigate with DataReceiver to: " + fxmlPath);

            java.net.URL resource = NavigationUtil.class.getResource(fxmlPath);
            if (resource == null) {
                String alt = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                resource = NavigationUtil.class.getClassLoader().getResource(alt);
            }
            if (resource == null) {
                String pref = fxmlPath.startsWith("/") ? ("resources" + fxmlPath) : ("resources/" + fxmlPath);
                resource = NavigationUtil.class.getClassLoader().getResource(pref);
            }
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);

            if (controllerInitializer != null) {
                loader.setControllerFactory(cls -> {
                    try {
                        Object controller = cls.getDeclaredConstructor().newInstance();
                        if (controller instanceof DataReceiver) {
                            try {
                                controllerInitializer.accept((DataReceiver) controller);
                            } catch (ClassCastException ex) {
                                System.out.println("⚠️ Controller initializer type mismatch: " + ex.getMessage());
                            }
                        }
                        return controller;
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to create controller instance: " + ex.getMessage(), ex);
                    }
                });
            }

            Parent root;
            try {
                root = loader.load();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Cannot load screen", "Error loading: " + fxmlPath + "\n\n" + ex.toString());
                return null;
            }

            DataReceiver controller = null;
            Object loadedController = loader.getController();
            if (loadedController instanceof DataReceiver) controller = (DataReceiver) loadedController;

            Stage stage = (sourceNode != null && sourceNode.getScene() != null) ?
                (Stage) sourceNode.getScene().getWindow() : new Stage();
            Scene scene = new Scene(root, width, height);

            try {
                String cssPath = NavigationUtil.class.getResource("/css/connect-theme.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                // ignore
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Navigation with DataReceiver successful to: " + title);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Cannot load screen", 
                "Error loading: " + fxmlPath + "\n\n" +
                "File might be missing or has syntax errors.\n" +
                "Error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Navigate to a screen with data
     */
    public static void navigateToWithData(String fxmlPath, String title, Node sourceNode, 
                                         int width, int height, Object data) {
        try {
            navigateTo(fxmlPath, title, sourceNode, width, height);
            
            // Give the controller time to initialize, then set data
            Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) sourceNode.getScene().getWindow();
                    Scene scene = stage.getScene();
                    Parent root = scene.getRoot();
                    
                    // Find the controller and pass data
                    if (root instanceof VBox) {
                        Node content = ((VBox) root).getChildren().stream()
                            .filter(node -> node instanceof ScrollPane)
                            .findFirst()
                            .orElse(null);
                        
                        if (content != null) {
                            Node actualRoot = ((ScrollPane) content).getContent();
                            if (actualRoot.getUserData() instanceof DataReceiver) {
                                DataReceiver controller = (DataReceiver) actualRoot.getUserData();
                                controller.receiveData(data);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error passing data to controller: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Navigation error: " + e.getMessage());
            throw new RuntimeException("Failed to navigate to " + fxmlPath, e);
        }
    }
    
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}