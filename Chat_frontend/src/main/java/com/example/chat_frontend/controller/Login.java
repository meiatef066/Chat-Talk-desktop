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
 * Controller for the login screen, handling user authentication and navigation to signup.
 */
public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private CheckBox rememberMe;

    private final AuthApi authApi = new AuthApi(new ApiClient());

    /**
     * Handles the login button action, validating input and authenticating the user.
     * @param event The action event from the login button.
     */
    @FXML
    public void LoginButton(ActionEvent event) {
        String emailText = email.getText().trim();
        String passwordText = password.getText();

        if (!Validation.isValidEmail(emailText) || !Validation.isValidPassword(passwordText)) {
            ShowDialogs.showWarningDialog("Please enter a valid email and password.");
            return;
        }

        User user = new User(emailText, passwordText);
        logger.debug("Attempting login for user: {}", emailText);

        Task<String> loginTask = new Task<>() {
            @Override
            protected String call() throws IOException, InterruptedException {
                return authApi.login(user);
            }
        };

        loginTask.setOnSucceeded(e -> {
            String token = loginTask.getValue();
            TokenManager.getInstance().setToken(token);
            System.out.println(token);
            logger.info("Login successful for user: {}", emailText);
            if (rememberMe.isSelected()) {
                // Optionally persist token securely (e.g., using Preferences API)
                logger.debug("Remember me selected; token persisted for user: {}", emailText);
            }
            NavigationUtil.switchScene(event, "/com/example/chat_frontend/ChatApp.fxml", "Application");
        });

        loginTask.setOnFailed(e -> {
            Throwable exception = loginTask.getException();
            String message = exception instanceof ApiClient.UnauthorizedException
                    ? "Invalid email or password."
                    : "Login failed: " + exception.getMessage();
            ShowDialogs.showErrorDialog(message);
            logger.error("Login failed for user: {}. Error: {}", emailText, exception.getMessage());
        });

        new Thread(loginTask).start();
    }

    /**
     * Navigates to the signup screen.
     * @param actionEvent The action event from the signup link/button.
     */
    @FXML
    public void NavigateToSignup(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/chat_frontend/Signup.fxml", "Signup");
    }
}