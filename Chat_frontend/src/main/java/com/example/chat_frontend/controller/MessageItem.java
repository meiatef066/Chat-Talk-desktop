package com.example.chat_frontend.controller;

import com.example.chat_frontend.DTO.MessageDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class MessageItem {
    @FXML private Label messageContent;
    @FXML private Label messageTimestamp;
    @FXML private HBox messageBox;

    public void setMessageData(MessageDTO message, boolean isSent) {
        messageContent.setText(message.getContent());
        messageTimestamp.setText(String.valueOf(message.getSentAt()));
        // Style the message based on whether it was sent or received
        messageBox.setStyle(isSent ?
                "-fx-background-color: #DCF8C6; -fx-background-radius: 5; -fx-alignment: center-right;" :
                "-fx-background-color: #FFFFFF; -fx-background-radius: 5; -fx-alignment: center-left;");
    }
}