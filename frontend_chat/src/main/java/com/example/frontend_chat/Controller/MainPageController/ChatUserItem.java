package com.example.frontend_chat.Controller.MainPageController;

import com.example.frontend_chat.DTO.SimpleUserDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ChatUserItem {
   @FXML
   public HBox chatItem;
    @FXML
    public ImageView avatar;
    @FXML
    public Label userName;
    @FXML
    public Label userEmail;
    @FXML
    public Label lastMessage;

    public void setUserData( SimpleUserDTO user, FriendList chatApp) {
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            avatar.setImage(new Image(user.getProfilePicture()));
        }
    }
}
