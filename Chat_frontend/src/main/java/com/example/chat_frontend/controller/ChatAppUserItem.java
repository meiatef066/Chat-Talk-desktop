package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.DTO.MessageDTO;
import com.example.chat_frontend.utils.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
public class ChatAppUserItem {
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Label lastMessage;
    @FXML private ImageView avatar;
    @FXML private HBox chatItem;
    private ChatApp chatApp;
    private final ChatAppApi api = new ChatAppApi();

    public void setUserData(SimpleUserDTO user, ChatApp chatApp) {
        this.chatApp = chatApp;
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());

        try {
            Long chatId = api.getOrCreatePrivateChat(user.getEmail(), TokenManager.getInstance().getEmail()).getId();
            List<MessageDTO> messages = api.getChatMessages(chatId);
            if (!messages.isEmpty()) {
                MessageDTO lastMsg = messages.get(messages.size() - 1);
                lastMessage.setText(lastMsg.getContent().substring(0, Math.min(lastMsg.getContent().length(), 30)) + "...");
            } else {
                lastMessage.setText("No messages yet");
            }
        } catch (IOException | InterruptedException e) {
            lastMessage.setText("Error loading message");
        }

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            avatar.setImage(new Image(user.getProfilePicture()));
        }

        chatItem.setOnMouseClicked(event -> {
            System.out.println("Clicked on: " + user.getFirstName() + " " + user.getLastName());
            chatApp.onFriendSelected(user);
        });
    }
}