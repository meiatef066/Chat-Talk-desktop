package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.APIRequests;
import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.Validation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Login {
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox rememberMe;

    @FXML
    public void LoginButton(ActionEvent event) {
        if (!Validation.isValidEmail(email.getText()) ||
            !Validation.isValidPassword(password.getText())) {
            return;
        }
        String email = this.email.getText();
        String password = this.password.getText();
        System.out.println(email+" "+password);
        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User(email, password);
        new Thread(() -> {
            try {
                String userJson = objectMapper.writeValueAsString(user);
                URL url = new URL("http://localhost:8080/api/auth/login");
                HttpURLConnection connection = APIRequests.POSTHttpURLConnection(url, userJson);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response JSON body (contains token)
                    try (var inputStream = connection.getInputStream()) {
                        String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        System.out.println("Response: " + responseBody);
                        // Parse JSON to extract token
                        var jsonNode = objectMapper.readTree(responseBody);
                        if (jsonNode.has("token")) {
                            String token = jsonNode.get("token").asText();
                            System.out.println("Token received: " + token);
                            // Save token to TokenManager singleton
                            com.example.chat_frontend.utils.TokenManager.getInstance().setToken(token);
                        } else {
                            System.err.println("Login response does not contain token!");
                            // Handle error: no token returned
                            return;
                        }
                    }
                    // Switch scene on UI thread after success
                    javafx.application.Platform.runLater(() -> {
                        NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
                    });
                } else {
                    System.out.println("Login failed! HTTP code: " + responseCode);
                    // You might want to show an error dialog here
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Handle exception, maybe show error dialog on UI thread
            }
        }).start();
    }

    @FXML
    public void NavigateToSignup(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/chat_frontend/Signup.fxml", "Signup");
    }
}
