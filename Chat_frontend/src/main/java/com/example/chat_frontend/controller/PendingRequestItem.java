package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.ApiResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingRequestItem {
    private static final Logger logger = LoggerFactory.getLogger(PendingRequestItem.class);

    @FXML public Button rejectBtn;
    @FXML public Button acceptBtn;
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private ImageView profileImage;

    private ContactResponse contactResponse;
    private ChatApp chatAppController;
    private final ChatAppApi api = new ChatAppApi();

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

    public void setChatAppController(ChatApp chatAppController) {
        this.chatAppController = chatAppController;
    }

    @FXML
    private void acceptRequest(ActionEvent event) {
        handleRequest("ACCEPTED");
    }

    @FXML
    private void rejectRequest(ActionEvent event) {
        handleRequest("REJECTED");
    }

    private void handleRequest(String action) {
        String senderEmail = contactResponse.getContact().getEmail();
        logger.info("Processing {} request for: {}", action, senderEmail);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ApiResponse response = api.respondToPendingRequest(senderEmail, action);
                    Platform.runLater(() -> {
                        chatAppController.getPendingRequests().remove(contactResponse);

                        if ("ACCEPTED".equalsIgnoreCase(action)) {
                            chatAppController.getFriends().add(contactResponse.getContact());
                            logger.info("Accepted request from {}", senderEmail);
                        } else {
                            logger.info("Rejected request from {}", senderEmail);
                        }

                        showAlert(Alert.AlertType.INFORMATION," response.getMessage()");
                    });
                } catch (Exception e) {
                    logger.error("Failed to {} request: {}", action, e.getMessage(), e);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Failed to " + action.toLowerCase() + " request: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
