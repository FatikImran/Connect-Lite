package com.connect;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.connect.service.EventService;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 400, 600);
        
        primaryStage.setTitle("Connect - Sign In");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        System.out.println("Application started successfully!");

        // Start periodic event status updater in background
        startPeriodicStatusUpdater();
    }

    private ScheduledExecutorService scheduler;

    private void startPeriodicStatusUpdater() {
        // Run update every 5 minutes; adjust as needed
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "event-status-updater");
            t.setDaemon(true);
            return t;
        });

        EventService eventService = new EventService();

        // Initial run async to avoid blocking startup
        scheduler.schedule(() -> {
            try {
                eventService.updateEventStatuses();
                System.out.println("Periodic event status updater: initial run complete");
            } catch (Exception e) {
                System.err.println("Error during initial event status update: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);

        // Regular interval runs
        scheduler.scheduleAtFixedRate(() -> {
            try {
                eventService.updateEventStatuses();
                System.out.println("Periodic event status updater: run complete");
            } catch (Exception e) {
                System.err.println("Error during periodic event status update: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        // Shutdown scheduler on app exit
        try {
            if (scheduler != null) scheduler.shutdownNow();
        } catch (Exception e) {
            System.err.println("Error shutting down scheduler: " + e.getMessage());
        }
        Platform.exit();
        super.stop();
    }
}