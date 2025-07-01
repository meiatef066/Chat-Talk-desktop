package com.example.chat_frontend.controller;

import com.example.chat_frontend.DTO.ContactRequest;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.Notifications.NotificationHandler;
import com.example.chat_frontend.Notifications.PopupNotificationManager;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationHandler notificationHandler = new PopupNotificationManager();
    private Long userId;

    public void setUserData( ContactResponse response) {
        SimpleUserDTO user=response.getContact();
        this.userId = user.getId();
        String displayedName=user.getFirstName()+" "+user.getLastName();
        userName.setText(displayedName);
        userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()
                && Validation.isValidUrl(user.getProfilePicture())) {
            profileImage.setImage(new Image(user.getProfilePicture(), true));
        }
        if(response.getStatus().equals("PENDING"))
        {
            addButton.setDisable(true);
            addButton.setText("Pending Request");
            addButton.setStyle("-fx-background-color: #595959; -fx-text-fill: white;");
        }
        if(response.getStatus().equals("ACCEPTED"))
        {
            addButton.setDisable(true);
            addButton.setText("Accepted");
            addButton.setStyle("-fx-background-color: #64fa7d; -fx-text-fill: white;");
        }
        if(response.getStatus().equals("BLOCKED")){
            addButton.setDisable(true);
            addButton.setText("Blocked");
            addButton.setStyle("-fx-background-color: #f45050; -fx-text-fill: white;");

        }
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        String receiverEmail = userEmail.getText();
        if (receiverEmail == null || receiverEmail.isEmpty()) {
            notificationHandler.displayErrorNotification("Invalid Action", "⚠️ Please select a valid user.");
            return;
        }

        addButton.setDisable(true);

        Task<Void> sendRequestTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                sendFriendRequest(receiverEmail);
                return null;
            }

            @Override
            protected void succeeded() {
                addButton.setText("Pending");
                addButton.setStyle("-fx-background-color: #ff8080; -fx-text-fill: white;");
                addButton.getStyleClass().remove("button-success");
                notificationHandler.displayMessageNotification("Request Sent", "✅ Friend request sent successfully!");
            }

            @Override
            protected void failed() {
                addButton.setDisable(false);
                Throwable e = getException();
                notificationHandler.displayErrorNotification("Send Failed", "❌ " + e.getMessage());
            }
        };

        new Thread(sendRequestTask).start();
    }

    private void sendFriendRequest(String receiverEmail) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8080/api/contacts/request");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());
            connection.setDoOutput(true);

            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setReceiver(receiverEmail);
            String jsonInputString = objectMapper.writeValueAsString(contactRequest);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Failed to send friend request. HTTP status: " + responseCode);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
