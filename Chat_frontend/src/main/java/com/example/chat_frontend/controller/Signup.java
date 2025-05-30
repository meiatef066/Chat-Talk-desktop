package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.APIRequests;
import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Signup {
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox agreeTermsAndConditions;

    @FXML
    public void CreateAccount( ActionEvent event ) {
        System.out.println("Create Account");
        User user = getAndValidData();
        if (user != null && !agreeTermsAndConditions.isSelected()) {
            ShowDialogs.showWarningDialog("Please agree to the terms and conditions ✌");
            return;
        }
        if (user != null) {
            ObjectMapper objectMapper = new ObjectMapper();

            //Run a separate thread for posting and getting an answer.
            new Thread(() -> {
                try {
                    String userJson = objectMapper.writeValueAsString(user);
                    URL url = new URL("http://localhost:8080/api/auth/signup");
                    HttpURLConnection connection = APIRequests.POSTHttpURLConnection(url, userJson);
                    int responseCode = connection.getResponseCode();

                    if (responseCode == 200 || responseCode == 201) {
                        // ✅ Read the response body (JSON)
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line.trim());
                            }
                        }

                        // ✅ Parse JSON and extract the token
                        ObjectMapper mapper = new ObjectMapper();
                        String responseBody = response.toString();
                        String token = mapper.readTree(responseBody).get("token").asText();

                        // ✅ Store the token
                        TokenManager.getInstance().setToken(token);
                        System.out.println("Token stored: " + token);

                        // ✅ Navigate to next scene
                        javafx.application.Platform.runLater(() -> {
                            NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
                        });

                    } else {
                        System.out.println("Signup failed: " + responseCode);
                        javafx.application.Platform.runLater(() -> {
                            ShowDialogs.showErrorDialog("Signup failed! Server responded with " + responseCode);
                        });
                    }

                } catch (IOException e) {
                    javafx.application.Platform.runLater(() -> {
                        ShowDialogs.showErrorDialog("Error: " + e.getMessage());
                    });
                    throw new RuntimeException(e);
                }
            }).start();

            System.out.println("api calling" + user);
        }
    }

    @FXML
    public void NavigateToLogin( ActionEvent event ) {
        NavigationUtil.switchScene(event, "/com/example/chat_frontend/Login.fxml", "Login");
    }

    // helper function
    private User getAndValidData() {
        if (!Validation.isValidName(firstName.getText()) || !Validation.isValidName(lastName.getText()) || !Validation.isValidEmail(email.getText()) || !Validation.isValidPassword(password.getText())) {
            return null;
        }
        return new User(firstName.getText(), lastName.getText(), email.getText(), password.getText());
    }


}
