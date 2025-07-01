package com.example.frontend_chat.NotificationService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NotificationPopupController {
   @FXML
   public AnchorPane root;
    @FXML
    public HBox contentBox;
    @FXML
    public ImageView icon;
    @FXML
    public Label title;
    @FXML
    public Label message;
    @FXML
    public HBox buttonBox;
    @FXML
    public Button acceptButton;
    @FXML
    public Button rejectButton;
    public VBox sidebar;
    public VBox profileSidebarItem;
    public VBox chatSidebarItem;
    public VBox sendSidebarItem;
    public VBox logoutSidebarItem;
    public VBox groupSidebarItem;
    @FXML private String senderEmail;
    public void setData( String title, String message, String iconPath, String notificationType, String senderEmail, Runnable onMessageClick ) {
        this.senderEmail = senderEmail;
        this.title.setText( title );
        this.message.setText( message );
        this.icon.setImage(new Image(getClass().getResource(iconPath).toExternalForm()));
        this.acceptButton.setDisable( false );
        this.rejectButton.setDisable( false );
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
//        chatAppApi.acceptFriendRequest(senderEmail);
    }

    @FXML
    private void handleReject() {
        System.out.println("Rejected friend request from: " + senderEmail);
//        chatAppApi.rejectFriendRequest(senderEmail); // Assuming ChatAppApi has this method
    }

    public void navigateToProfile( MouseEvent event ) {

    }

    public void navigateToChat( MouseEvent event ) {

    }

    public void navigateToGroup( MouseEvent event ) {

    }

    public void navigateToSend( MouseEvent event ) {

    }

    public void logout( MouseEvent event ) {

    }
}
