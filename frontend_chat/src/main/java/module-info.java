module com.example.frontend_chat {
    requires javafx.controls;
    requires javafx.fxml;

//    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires org.slf4j;
    requires javafx.media;
    requires com.fasterxml.jackson.databind;
    requires spring.web;
    requires spring.core;
    requires spring.boot.starter;
    requires spring.websocket;
    requires Java.WebSocket;
    requires spring.messaging;
    exports com.example.frontend_chat.DTO.Enum; // Export Enum package containing C
    opens com.example.frontend_chat.Controller.AuthController to javafx.fxml;
    opens com.example.frontend_chat to javafx.fxml;
    exports com.example.frontend_chat;
    exports com.example.frontend_chat.utils;
    exports com.example.frontend_chat.Controller.AuthController to javafx.fxml;
    opens com.example.frontend_chat.NotificationService to javafx.fxml;
    opens com.example.frontend_chat.DTO to com.fasterxml.jackson.databind;
    exports com.example.frontend_chat.DTO;
    exports com.example.frontend_chat.Controller.MainPageController;
    opens com.example.frontend_chat.Controller.MainPageController to javafx.fxml;
    // Export the Controller package to javafx.fxml
    exports com.example.frontend_chat.Controller to javafx.fxml;
    opens com.example.frontend_chat.Controller.SearchController to javafx.fxml;

    // Open the Controller package to  reflective access by javafx.fxml
    opens com.example.frontend_chat.Controller to javafx.fxml;

}