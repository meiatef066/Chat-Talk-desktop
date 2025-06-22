module com.example.chat_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires spring.boot.autoconfigure;
    requires java.net.http;
    requires static lombok;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires spring.websocket;
    requires spring.messaging;
    requires spring.core;
    requires Java.WebSocket;
    requires jakarta.annotation;
    requires spring.web;

    opens com.example.chat_frontend.DTO to com.fasterxml.jackson.databind; // Allow Jackson to access DTO
//    exports com.example.chat_frontend.controller;
    exports com.example.chat_frontend.DTO to com.fasterxml.jackson.databind; // Export DTO to Jackson

    opens com.example.chat_frontend.Model to com.fasterxml.jackson.databind;
    opens com.example.chat_frontend.controller to javafx.fxml;
    opens com.example.chat_frontend to javafx.fxml;
    exports com.example.chat_frontend;
}