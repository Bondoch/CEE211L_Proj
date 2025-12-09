package com.example.triage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.triage.services.UserService;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {

            FXMLLoader loader;

            if (UserService.isUserTableEmpty()) {
                loader = new FXMLLoader(getClass().getResource("/com/triage/app/views/setup-admin.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/com/example/triage/views/login-view.fxml"));
            }

            Scene scene = new Scene(loader.load(), 900, 600);
            stage.setTitle("Triage System");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
