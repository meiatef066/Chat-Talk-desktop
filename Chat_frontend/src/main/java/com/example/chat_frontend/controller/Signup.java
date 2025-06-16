package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ApiClient;
import com.example.chat_frontend.API.AuthApi;
import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller for the signup screen, handling user registration and navigation to login.
 */
public class Signup {
    private static final Logger logger = LoggerFactory.getLogger(Signup.class);

    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private PasswordField passwordConfirm;
    @FXML private CheckBox agreeTermsAndConditions;

    private final AuthApi authApi = new AuthApi(new ApiClient());

    /**
     * Handles the create account button action, validating input and registering the user.
     * @param event The action event from the create account button.
     */
    @FXML
    public void CreateAccount(ActionEvent event) {
        User user = getAndValidData();
        if (user == null) {
            ShowDialogs.showWarningDialog("Please enter valid first name, last name, email, password, and confirm password.");
            return;
        }
        if (!agreeTermsAndConditions.isSelected()) {
            ShowDialogs.showWarningDialog("Please agree to the terms and conditions.");
            return;
        }

        logger.debug("Attempting signup for user: {}", user.getEmail());

        Task<String> signupTask = new Task<>() {
            @Override
            protected String call() throws IOException, InterruptedException {
                return authApi.signup(user);
            }
        };

        signupTask.setOnSucceeded(e -> {
            String token = signupTask.getValue();
            TokenManager.getInstance().setToken(token);
            logger.info("Signup successful for user: {}", user.getEmail());
            NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
        });

        signupTask.setOnFailed(e -> {
            Throwable exception = signupTask.getException();
            String message = exception instanceof ApiClient.UnauthorizedException
                    ? "Email already in use or invalid data."
                    : "Signup failed: " + exception.getMessage();
            ShowDialogs.showErrorDialog(message);
            logger.error("Signup failed for user: {}. Error: {}", user.getEmail(), exception.getMessage());
        });

        new Thread(signupTask).start();
    }

    /**
     * Navigates to the login screen.
     * @param event The action event from the login link/button.
     */
    @FXML
    public void NavigateToLogin(ActionEvent event) {
        NavigationUtil.switchScene(event, "/com/example/chat_frontend/Login.fxml", "Login");
    }

    /**
     * Validates input data and creates a User object if valid.
     * @return User object if valid, null otherwise.
     */
    private User getAndValidData() {
        String firstNameText = firstName.getText().trim();
        String lastNameText = lastName.getText().trim();
        String emailText = email.getText().trim();
        String passwordText = password.getText();
        String passwordConfirmText = passwordConfirm.getText();

        if (!Validation.isValidName(firstNameText) ||
                !Validation.isValidName(lastNameText) ||
                !Validation.isValidEmail(emailText) ||
                !Validation.isValidPassword(passwordText) ||
                !passwordText.equals(passwordConfirmText)) {
            return null;
        }

        return new User(firstNameText, lastNameText, emailText, passwordText);
    }
}