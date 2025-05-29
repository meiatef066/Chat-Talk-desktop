package com.example.chat_frontend.controller;

import com.example.chat_frontend.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SidebarNavigator {

    @FXML private VBox sidebar;
    @FXML private VBox profileSidebarItem;
    @FXML private VBox chatSidebarItem;
    @FXML private VBox groupSidebarItem;
    @FXML private VBox sendSidebarItem;
    @FXML private VBox logoutSidebarItem;

    @FXML
    public void initialize() {
        // Ensure Profile is highlighted by default
        highlightItem(chatSidebarItem);
    }

    @FXML
    private void navigateToProfile(MouseEvent mouseEvent) {
        highlightItem(profileSidebarItem);
        NavigationUtil.switchScene(mouseEvent, "/com/example/chat_frontend/Profile.fxml", "Profile");
    }

    @FXML
    private void navigateToChat(MouseEvent mouseEvent) {
        highlightItem(chatSidebarItem);
        NavigationUtil.switchScene(mouseEvent, "/com/example/chat_frontend/ChatApp.fxml", "Chat&Talk");
    }

    @FXML
    private void navigateToGroup(MouseEvent mouseEvent) {
        highlightItem(groupSidebarItem);
        NavigationUtil.switchScene(mouseEvent, "/com/example/chat_frontend/Group.fxml", "Group");
    }

    @FXML
    private void navigateToSend(MouseEvent mouseEvent) {
        highlightItem(sendSidebarItem);
        NavigationUtil.switchScene(mouseEvent, "/com/example/chat_frontend/AddUser.fxml", "send invitation ");
    }

    @FXML
    private void logout(MouseEvent mouseEvent) {
        highlightItem(logoutSidebarItem);
        // Clear token (implement as needed)
        NavigationUtil.switchScene(mouseEvent, "/com/example/chat_frontend/Login.fxml", "Login");
    }

    private void highlightItem(VBox selectedItem) {
        // Clear active style from all items
        profileSidebarItem.getStyleClass().remove("sidebar-item-active");
        chatSidebarItem.getStyleClass().remove("sidebar-item-active");
        groupSidebarItem.getStyleClass().remove("sidebar-item-active");
        sendSidebarItem.getStyleClass().remove("sidebar-item-active");
        logoutSidebarItem.getStyleClass().remove("sidebar-item-active");

        // Apply active style to selected item
        selectedItem.getStyleClass().add("sidebar-item-active");
    }
}