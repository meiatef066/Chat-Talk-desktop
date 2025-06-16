package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.UserSearchDTO;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AddUserItem {

    @FXML
    private Label userName;
    @FXML
    private Label userEmail;
    @FXML
    private ImageView profileImage;
    @FXML
    private Button addButton;

    private static final String DEFAULT_IMAGE = "/images/icons2/icons8-male-user-50.png";
    private Long userId;

    // Initialize the UI with UserSearchDTO data
    public void setUserData(UserSearchDTO user) {
        this.userId = user.getId();
        userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown");
        userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()&& Validation.isValidUrl(user.getAvatarUrl())) {
            profileImage.setImage(new Image(user.getAvatarUrl(), true));
        }
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        if (userId == null) {
            Platform.runLater(() -> ShowDialogs.showErrorDialog("Cannot add user: Invalid user ID"));
            return;
        }
        // Perform API call in a background thread
        new Thread(() -> {
            try {
                // Construct the URL
                URL url = new URL("http://localhost:8080/api/contacts");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());
                connection.setDoOutput(true);

                // Create JSON payload
                String jsonInputString = "{\"userId\": " + userId + "}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Platform.runLater(() -> ShowDialogs.showErrorDialog("Failed to add user: HTTP " + responseCode));
                    return;
                }

                connection.disconnect();
                Platform.runLater(() -> {
                    ShowDialogs.showInfoDialog("User added successfully!");
                    addButton.setDisable(true); // Disable button after adding
                    addButton.setStyle(String.valueOf(Color.red));
                    addButton.setText("Pending");
                });

            } catch (IOException e) {
                Platform.runLater(() -> ShowDialogs.showErrorDialog("Error adding user: " + e.getMessage()));
            }
        }).start();
    }
}