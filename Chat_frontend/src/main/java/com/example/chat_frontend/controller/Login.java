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
                HttpURLConnection connection= APIRequests.POSTHttpURLConnection(url, userJson);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Signup successful!");
                    javafx.application.Platform.runLater(() -> {
                        NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
                    });
                }else {
                    System.out.println("Signup failed!");
                    javafx.application.Platform.runLater(() -> {
                        NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    @FXML
    public void NavigateToSignup(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/chat_frontend/Signup.fxml", "Signup");
    }
}
