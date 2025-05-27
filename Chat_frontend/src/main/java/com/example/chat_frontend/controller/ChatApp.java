package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatApp {
    @FXML
    private VBox userListVBox; // This must match fx:id="userListVBox" in ChatApp.fxml

    public void initialize() {
        List<User> users = List.of(
                new User("Alice", "Smith", "https://i.pravatar.cc/40?img=1"),
                new User("Bob", "Johnson", "https://i.pravatar.cc/40?img=2"),
                new User("Charlie", "Brown", "https://i.pravatar.cc/40?img=3")
        );

        for (User user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/ChatUserItem.fxml"));                Node node = loader.load();
                ChatUserItem controller = loader.getController();
                controller.setUserData(user, " ");
                userListVBox.getChildren().add(node);            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}