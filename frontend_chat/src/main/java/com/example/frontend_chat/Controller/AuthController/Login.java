package com.example.frontend_chat.Controller.AuthController;

import com.example.frontend_chat.API.AuthAPI.AuthAPI;
import com.example.frontend_chat.DTO.LoginRequest;
import com.example.frontend_chat.NotificationService.NotificationHandler;
import com.example.frontend_chat.NotificationService.NotificationPopup;
import com.example.frontend_chat.utils.NavigationUtil;
import com.example.frontend_chat.utils.TokenManager;
import com.example.frontend_chat.utils.Validation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login {
   @FXML public TextField email;
   @FXML public PasswordField password;
   @FXML public CheckBox rememberMe;
   NotificationHandler notificationPopup=new NotificationPopup();

    @FXML
    public void NavigateToSignup( ActionEvent actionEvent ) {
        NavigationUtil.switchScene(actionEvent,"/com/example/frontend_chat/auth/Signup.fxml","Signup 🤖");
        System.out.println("nav to sign up");
    }
    @FXML
    public void LoginButton( ActionEvent actionEvent ) {
    LoginRequest user=ValidateInputField();
        if(user!=null){
            AuthAPI.loginAsync(user,
                    token -> {
                        System.out.println("✅ Login successful: " + token);
                        TokenManager.getInstance().setToken(token,email.getText());
                        notificationPopup.displayMessageNotification("Welcome "+email.getText(),"login successfully");
                        NavigationUtil.switchScene(actionEvent,"/com/example/frontend_chat/MainPage/ChatApp.fxml","Chat & Talk 🦜");
                    },
                    error -> {
                        System.out.println("❌ Login failed: " + error);
                        notificationPopup.displayErrorNotification("Error ","error occur");
                    }
            );
        }

    }
    private LoginRequest ValidateInputField() {
        if (Validation.isValidEmail(email.getText())
           &&Validation.isValidPassword(password.getText())){
            return new LoginRequest(email.getText(),password.getText());
        }
        notificationPopup.displayErrorNotification("Error","Error in email or password");
        return null;
    }
}
