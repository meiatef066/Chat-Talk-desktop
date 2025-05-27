module com.example.chat_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens com.example.chat_frontend.controller to javafx.fxml;
    opens com.example.chat_frontend to javafx.fxml;
    exports com.example.chat_frontend;
}