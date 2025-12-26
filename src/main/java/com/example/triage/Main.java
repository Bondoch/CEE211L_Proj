package com.example.triage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import com.example.triage.services.CapacityMonitor;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/login-view.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setResizable(true);

            //  Load system name from preferences
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Main.class);
            String systemName = prefs.get("systemName", "LifeLine Triage System");
            stage.setTitle(systemName);

            //  INITIALIZE CAPACITY MONITORING
            CapacityMonitor.getInstance().setPrimaryStage(stage);
            System.out.println("✅ Capacity monitoring initialized");

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //  CLEAN SHUTDOWN
    @Override
    public void stop() {
        System.out.println("🛑 Application shutting down...");
        CapacityMonitor.getInstance().stopMonitoring();
        System.out.println("✅ Capacity monitoring stopped");
    }
    public static void main(String[] args) {
        launch();
    }
}