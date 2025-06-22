package com.example.chat_frontend.controller;

import com.example.chat_frontend.DTO.ContactRequest;
import com.example.chat_frontend.Model.UserSearchDTO;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private final ObjectMapper objectMapper = new ObjectMapper(); // Initialize ObjectMapper
    private Long userId;

    // Initialize the UI with UserSearchDTO data
    public void setUserData(UserSearchDTO user) {
        this.userId = user.getId();
        userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown");
        userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty() && Validation.isValidUrl(user.getAvatarUrl())) {
            profileImage.setImage(new Image(user.getAvatarUrl(), true));
        }
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        if (userEmail.getText() == null || userEmail.getText().isEmpty()) {
            Platform.runLater(() -> {
                ShowDialogs.showErrorDialog("⚠️ Please select a valid user.");
                addButton.setDisable(false);
            });
            return;
        }
        addButton.setDisable(true);
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/api/contacts/request");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());
                connection.setDoOutput(true);

                ContactRequest contactRequest = new ContactRequest();
                contactRequest.setReceiver(userEmail.getText());
           //   Serialize to JSON using ObjectMapper
                String jsonInputString = objectMapper.writeValueAsString(contactRequest);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    Platform.runLater(() -> {
                        ShowDialogs.showInfoDialog("✅ Friend request sent successfully!");
                        addButton.setText("Pending");
                        addButton.setStyle("-fx-background-color: #ff8080; -fx-text-fill: white;");
                        addButton.getStyleClass().remove("button-success");
                    });
                } else {
                    Platform.runLater(() -> {
                        ShowDialogs.showErrorDialog("❌ Failed to send request: HTTP " + responseCode);
                        addButton.setDisable(false);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    ShowDialogs.showErrorDialog("⚠️ Error sending request: " + e.getMessage());
                    addButton.setDisable(false);
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}