package com.example.frontend_chat.Controller;

import com.example.frontend_chat.utils.NavigationUtil;
import com.example.frontend_chat.utils.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class SidebarNavigator {

    @FXML
    public void initialize(){

    }
    @FXML
    public void navigateToProfile( MouseEvent event ) {
        NavigationUtil.switchScene(event, "/com/example/frontend_chat/auth/Profile.fxml", "Login 🤖");
    }
    @FXML
    public void navigateToChat( MouseEvent event ) {
        NavigationUtil.switchScene(event, "/com/example/frontend_chat/MainPage/ChatApp.fxml", "Chat & Talk 🦜");
    }
    @FXML
    public void navigateToGroup( MouseEvent event ) {
        NavigationUtil.switchScene(event, "/com/example/frontend_chat/GroupPage/Group.fxml", "Groups 🕹");
    }
    @FXML
    public void navigateToSend( MouseEvent event ) {
        NavigationUtil.switchScene(event, "/com/example/frontend_chat/SearchPage/AddUser.fxml", "Search 🔍");
    }
    @FXML
    public void logout( MouseEvent event ) {
        TokenManager.getInstance().clearToken();
        System.out.println("Logged out successfully and deleted token");
        NavigationUtil.switchScene(event, "/com/example/frontend_chat/auth/Login.fxml", "Login");
    }
}
