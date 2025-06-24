package com.example.chat_frontend.Notifications;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.Objects;

public class NotificationPopupController {

    public HBox contentBox;
    @FXML private AnchorPane root;
    @FXML private Label title;
    @FXML private Label message;
    @FXML private ImageView icon;
    @FXML private HBox buttonBox;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;

    private String senderEmail;
    private Runnable onMessageClick;

    public void setData(String titleText, String messageText, String iconPath, String notificationType, String senderEmail, Runnable onMessageClick) {
        this.senderEmail = senderEmail;
        this.onMessageClick = onMessageClick;

        title.setText(titleText);
        message.setText(messageText);

        // Ensure icon path starts with a leading slash and is relative to resources root
        String resolvedIconPath = iconPath.startsWith("/") ? iconPath : "/" + iconPath;
        String resolvedDefaultIconPath = "/images/icons/default.png";

        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(resolvedIconPath)));
            if (image.isError()) {
                throw new Exception("Image load failed");
            }
            icon.setImage(image);
        } catch (Exception e) {
            System.err.println("⚠️ Error loading icon: " + resolvedIconPath + " - " + e.getMessage());
            try {
                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(resolvedDefaultIconPath)));
                if (!defaultImage.isError()) {
                    icon.setImage(defaultImage);
                } else {
                    System.err.println("⚠️ Error loading default icon: " + resolvedDefaultIconPath);
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error loading default icon: " + ex.getMessage());
            }
        }

        root.getStyleClass().clear();
        root.getStyleClass().add("root");
        switch (notificationType) {
            case "MESSAGE":
                root.getStyleClass().add("bg-info");
                root.setOnMouseClicked(event -> {
                    if (onMessageClick != null) onMessageClick.run();
                });
                break;
            case "ERROR":
                root.getStyleClass().add("bg-danger");
                break;
            case "FRIEND_REQUEST":
                root.getStyleClass().add("bg-warning");
                buttonBox.setVisible(true);
                buttonBox.setManaged(true);
                break;
            case "FRIEND_RESPONSE":
                root.getStyleClass().add("bg-success");
                break;
            default:
                root.getStyleClass().add("bg-info");
        }
    }

    @FXML
    private void handleAccept() {
        System.out.println("Accepted friend request from: " + senderEmail);
        // TODO: Implement server API call to accept friend request
    }

    @FXML
    private void handleReject() {
        System.out.println("Rejected friend request from: " + senderEmail);
        // TODO: Implement server API call to reject friend request
    }
}