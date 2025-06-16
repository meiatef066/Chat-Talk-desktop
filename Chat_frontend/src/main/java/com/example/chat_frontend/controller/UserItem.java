package com.example.chat_frontend.controller;

import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.utils.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class UserItem {
    @FXML
    private Label userName;
    @FXML
    private Label userEmail;
    @FXML
    private ImageView profileImage;

    public void setUserData( SimpleUserDTO userDTO ) {
        userName.setText(userDTO.getFirstName()+ " " + userDTO.getLastName());
        userEmail.setText(userDTO.getEmail());
        if (userDTO.getProfilePicture()!= null && !userDTO.getProfilePicture().isEmpty()&& Validation.isValidUrl(userDTO.getProfilePicture())) {
            profileImage.setImage(new Image(userDTO.getProfilePicture(),true));
        }
    }
}
