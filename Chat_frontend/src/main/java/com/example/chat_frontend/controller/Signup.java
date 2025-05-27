package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javax.swing.*;

public class Signup {
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox agreeTermsAndConditions;
    @FXML
    public void CreateAccount( ActionEvent event) {
       User user = getAndValidData();
        if(user!=null&&!agreeTermsAndConditions.isSelected()) {
            ShowDialogs.showWarningDialog("please, read our term and condition and select checkbox if you agree ✌ ");
            return;
        }
        if(user != null) {
            // call backend api
            System.out.println("api calling"+user);
            NavigationUtil.switchSceneWithFade(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");

        }
    }
    @FXML
    public void NavigateToLogin(ActionEvent event) {
        NavigationUtil.switchSceneWithFade(event, "/com/example/chat_frontend/Login.fxml", "Login");

    }
    // helper function
    private User getAndValidData() {
        if(firstName.getText().isEmpty() || lastName.getText().isEmpty() || email.getText().isEmpty() || password.getText().isEmpty()) {
            ShowDialogs.showWarningDialog("Please, enter all field");
            return null;
        }
        User user = new User(firstName.getText(), lastName.getText(), email.getText(), password.getText());
        return user;
    }

}
