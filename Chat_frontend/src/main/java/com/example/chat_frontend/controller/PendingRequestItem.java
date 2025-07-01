package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PendingRequestItem {
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private HBox requestItem;
    private ChatApp chatApp;
    private final ChatAppApi api = new ChatAppApi ();

    public void setUserData(ContactResponse contactResponse) {
        SimpleUserDTO user = contactResponse.getContact();
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());

        acceptButton.setOnAction(event -> {
            api.acceptFriendRequest(user.getEmail());
            chatApp.handleFriendResponse(contactResponse);
        });

        rejectButton.setOnAction(event -> {
            api.rejectFriendRequest(user.getEmail());
            chatApp.handleFriendResponse(contactResponse);
        });
    }

    public void setChatAppController(ChatApp chatApp) {
        this.chatApp = chatApp;
    }
}