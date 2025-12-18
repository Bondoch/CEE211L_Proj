package com.example.triage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import com.example.triage.services.UserService;
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {

            FXMLLoader loader;

            if (UserService.isUserTableEmpty()) {
                loader = new FXMLLoader(
                        getClass().getResource("/com/triage/app/views/setup-admin.fxml")
                );
            } else {
                loader = new FXMLLoader(
                        getClass().getResource("/com/example/triage/views/login-view.fxml")
                );
            }

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setResizable(true);

            // ✅ Load system name from preferences
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Main.class);
            String systemName = prefs.get("systemName", "LifeLine Triage System");
            stage.setTitle(systemName);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
