package com.example.frontend_chat.Controller.AuthController;

import com.example.frontend_chat.API.AuthAPI.AuthAPI;
import com.example.frontend_chat.DTO.RegisterRequest;
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

public class Signup {
    @FXML
    public TextField firstName;
    @FXML
    public TextField lastName;
    @FXML
    public TextField email;
    @FXML
    public PasswordField password;
    @FXML
    public PasswordField passwordConfirm;
    @FXML
    public CheckBox agreeTermsAndConditions;
    NotificationHandler notificationPopup = new NotificationPopup();

    public void NavigateToLogin( ActionEvent actionEvent ) {
//        com/example/frontend_chat/auth/Login.fxml
        NavigationUtil.switchScene(actionEvent, "/com/example/frontend_chat/auth/Login.fxml", "Signup 🤖");

    }

    public void CreateAccount( ActionEvent actionEvent ) {
        RegisterRequest user = ValidateInputField();
        if (user != null) {
            AuthAPI.signupAsync(user,
                    token -> {
                        System.out.println("✅ sign up successful: " + token);
                        TokenManager.getInstance().setToken(token, email.getText());
                        notificationPopup.displayMessageNotification("Welcome " + firstName.getText(), "Account created successfully");
                        NavigationUtil.switchScene(actionEvent, "/com/example/frontend_chat/MainPage/ChatApp.fxml", "Chat & Talk 🦜");
                    },
                    error -> {
                        System.out.println("❌ Login failed: " + error);
                        notificationPopup.displayErrorNotification("Error ", "error occur");
                    }
            );
        }
    }

    private RegisterRequest ValidateInputField() {
        if (!agreeTermsAndConditions.isSelected()) {
            notificationPopup.displayErrorNotification("Information", "Please read terms & condition and select checkbox");
            return null;
        }
        if (Validation.isValidEmail(email.getText()) && Validation.isValidPassword(password.getText())
                && Validation.isValidPassword(passwordConfirm.getText())
                && Validation.isValidName(firstName.getText()
        ) && Validation.isValidName(lastName.getText())) {
            return new RegisterRequest(firstName.getText(), lastName.getText(), email.getText(), password.getText());
        }
        return null;
    }
}
