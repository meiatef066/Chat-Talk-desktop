package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ChatUserItem {
    @FXML
    private Label userName;

    @FXML
    private Label lastMessage;

    @FXML
    private ImageView avatar;

    @FXML
    private HBox chatItem;

    public void setUserData(User user, String avatarUrl) {
        userName.setText(user.getFirstName() + " " + user.getLastName());
        lastMessage.setText("last message");
//
//        if (avatarUrl != null && !avatarUrl.isEmpty()) {
//            avatar.setImage(new Image(avatarUrl));
//        } else {
//            avatar.setImage(new Image("https://asset.cloudinary.com/dvghbsyda/4ed06177fc4a01335e52c41cf580447c"));
//        }

        // Set click action on the whole item (root node)
        chatItem.setOnMouseClicked(event -> {
            System.out.println("Clicked on: " + user.getFirstName() + " " + user.getLastName());
            // TODO: Open conversation window
        });
    }
}