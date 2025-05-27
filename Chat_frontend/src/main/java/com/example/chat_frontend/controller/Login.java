package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login {
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox rememberMe;

    @FXML
    public void LoginButton() {
        if (email.getText().equals("") || password.getText().equals("")) {
            ShowDialogs.showWarningDialog("please enter email and password");
        }
        String email = this.email.getText();
        String password = this.password.getText();
        System.out.println(email+" "+password);
//        User user = new User( email, password);
//        System.out.println(user.toString());
//
//        try {
//            // Serialize the User object to JSON
//            ObjectMapper objectMapper = new ObjectMapper();
//            String json = objectMapper.writeValueAsString(user); // Serialize User object
//
//            // Print the JSON string to verify
//            System.out.println(json);
//
//            // send json
//            URL url =new URL("http://localhost:8080/api/customers/login");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setDoOutput(true);
//
//            connection.getOutputStream().write(json.getBytes());
//
//            // get response
//            int responseCode = connection.getResponseCode();
//            System.out.println("response is : "+responseCode);
//            if (responseCode == 200) {
//                ToPagefun("mainPage");
//            }else
//            {
//                System.out.println("not corrrect");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace(); // Print any exception to the console
//        }

    }

    @FXML
    public void NavigateToSignup(ActionEvent actionEvent) {
        NavigationUtil.switchSceneWithFade(actionEvent, "/com/example/chat_frontend/Signup.fxml", "Signup");
    }
}
