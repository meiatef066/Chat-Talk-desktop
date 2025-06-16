package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ApiClient;
import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.utils.Validation;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

@Getter
public class PendingRequestItem {
    @FXML
    public Button rejectBtn;
    @FXML
    public Button acceptBtn;
    @FXML
    private Label userName;
    @FXML
    private Label userEmail;
    @FXML
    private ImageView profileImage;
    @Getter
    private ContactResponse contactResponse;
    private final ChatAppApi api = new ChatAppApi(new ApiClient());
    private ChatApp chatAppController; // Reference to ChatApp controller

    // Method to set the parent controller
    public void setChatAppController(ChatApp chatAppController) {
        this.chatAppController = chatAppController;
    }

    @FXML
    public void setUserData(ContactResponse contactResponse) {
        this.contactResponse = contactResponse;
        userName.setText(contactResponse.getContact().getFirstName() + " " + contactResponse.getContact().getLastName());
        userEmail.setText(contactResponse.getContact().getEmail());
        if (contactResponse.getContact().getProfilePicture() != null &&
                !contactResponse.getContact().getProfilePicture().isEmpty() &&
                Validation.isValidUrl(contactResponse.getContact().getProfilePicture())) {
            profileImage.setImage(new Image(contactResponse.getContact().getProfilePicture(), true));
        }
    }

    @FXML
    public void acceptRequest(ActionEvent actionEvent) {
        System.out.println("Accepting request for: " + contactResponse.getContact().getEmail());
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String response = api.responseToPendingRequest("/contacts/" + contactResponse.getContact().getEmail() + "/response?action=ACCEPTED");
                    System.out.println("API Response: " + response);
                    Platform.runLater(() -> {
                        // Remove from pending requests
                        chatAppController.getPendingRequests().remove(contactResponse);
                        // Add to friend list
                        chatAppController.getFriends().add(contactResponse.getContact());
                        // Notify user (optional)
                        showAlert(Alert.AlertType.INFORMATION, "Friend request accepted for " + contactResponse.getContact().getEmail());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Failed to accept request: " + e.getMessage());
                    });
                    throw e;
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void rejectRequest(ActionEvent actionEvent) {
        System.out.println("Rejecting request for: " + contactResponse.getContact().getEmail());
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String response = api.responseToPendingRequest("/contacts/" + contactResponse.getContact().getEmail() + "/response?action=REJECTED");
                    System.out.println("API Response: " + response);
                    Platform.runLater(() -> {
                        // Remove from pending requests
                        chatAppController.getPendingRequests().remove(contactResponse);
                        // Notify user (optional)
                        showAlert(Alert.AlertType.INFORMATION, "Friend request rejected for " + contactResponse.getContact().getEmail());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Failed to reject request: " + e.getMessage());
                    });
                    throw e;
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}