package com.example.chat_frontend.controller;

import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SidebarNavigator {

    @FXML private VBox sidebar;
    @FXML private VBox profileSidebarItem;
    @FXML private VBox chatSidebarItem;
    @FXML private VBox groupSidebarItem;
    @FXML private VBox sendSidebarItem;
    @FXML private VBox logoutSidebarItem;

    private VBox activeItem;

    @FXML
    public void initialize() {
        // Initialize tooltips for accessibility
        Tooltip.install(profileSidebarItem, new Tooltip("View your profile"));
        Tooltip.install(chatSidebarItem, new Tooltip("Open chat"));
        Tooltip.install(groupSidebarItem, new Tooltip("View groups"));
        Tooltip.install(sendSidebarItem, new Tooltip("Send invitation"));
        Tooltip.install(logoutSidebarItem, new Tooltip("Log out"));

        // Set default active item
        highlightItem(chatSidebarItem);

        // Add keyboard navigation support
        sidebar.setOnKeyPressed(this::handleKeyNavigation);
    }

    @FXML
    private void navigateToProfile(MouseEvent mouseEvent) {
        navigateToScene(mouseEvent, profileSidebarItem, "/com/example/chat_frontend/Profile.fxml", "Profile");
    }

    @FXML
    private void navigateToChat(MouseEvent mouseEvent) {
        navigateToScene(mouseEvent, chatSidebarItem, "/com/example/chat_frontend/ChatApp.fxml", "Chat&Talk");
    }

    @FXML
    private void navigateToGroup(MouseEvent mouseEvent) {
        navigateToScene(mouseEvent, groupSidebarItem, "/com/example/chat_frontend/Group.fxml", "Group");
    }

    @FXML
    private void navigateToSend(MouseEvent mouseEvent) {
        navigateToScene(mouseEvent, sendSidebarItem, "/com/example/chat_frontend/AddUser.fxml", "Send Invitation");
    }

    @FXML
    private void logout(MouseEvent mouseEvent) {
        highlightItem(logoutSidebarItem);
        TokenManager.getInstance().clearToken();
        System.out.println("Logged out successfully and deleted token");
        navigateToScene(mouseEvent, null, "/com/example/chat_frontend/Login.fxml", "Login");
    }

    private void navigateToScene(MouseEvent mouseEvent, VBox item, String fxmlPath, String title) {
        try {
            if (item != null) {
                highlightItem(item);
            }
            NavigationUtil.switchScene(mouseEvent, fxmlPath, title);
        } catch (Exception e) {
            System.err.println("Failed to navigate to " + title + ": " + e.getMessage());
            // Optionally show an alert to the user
        }
    }

    private void highlightItem(VBox selectedItem) {
        if (activeItem != null) {
            activeItem.getStyleClass().remove("sidebar-item-active");
        }
        if (selectedItem != null) {
            selectedItem.getStyleClass().add("sidebar-item-active");
            activeItem = selectedItem;
        }
    }

    private void handleKeyNavigation(KeyEvent event) {
        VBox[] items = {profileSidebarItem, chatSidebarItem, groupSidebarItem, sendSidebarItem, logoutSidebarItem};
        int currentIndex = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == activeItem) {
                currentIndex = i;
                break;
            }
        }

        if (event.getCode() == KeyCode.UP && currentIndex > 0) {
            highlightItem(items[currentIndex - 1]);
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN && currentIndex < items.length - 1) {
            highlightItem(items[currentIndex + 1]);
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            if (activeItem == profileSidebarItem) navigateToProfile(null);
            else if (activeItem == chatSidebarItem) navigateToChat(null);
            else if (activeItem == groupSidebarItem) navigateToGroup(null);
            else if (activeItem == sendSidebarItem) navigateToSend(null);
            else if (activeItem == logoutSidebarItem) logout(null);
            event.consume();
        }
    }
}