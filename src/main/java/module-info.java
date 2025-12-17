module com.example.triage {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    opens com.example.triage to javafx.fxml;
    opens com.example.triage.controllers to javafx.fxml;

    exports com.example.triage;
    exports com.example.triage.controllers;
    exports com.example.triage.services;

    opens com.example.triage.services to javafx.fxml;
}