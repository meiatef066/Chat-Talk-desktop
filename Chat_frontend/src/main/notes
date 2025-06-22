package com.example.chat_frontend.controller;

import com.example.chat_frontend.Model.UserSearchDTO;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AddUser {

    @FXML
    private TextField contactSearchField;
    @FXML
    private VBox searchResultsVBox;
    @FXML
    private VBox emptyState;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label noResultsLabel;

    @FXML
    public void initialize() {
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        loadingIndicator.setVisible(false);
        searchResultsVBox.getChildren().clear();
        searchResultsVBox.getChildren().addAll(emptyState, loadingIndicator);
    }

    @FXML
    public void searchUsers(KeyEvent keyEvent) {
        String searchText = contactSearchField.getText().trim();
        if (searchText.isEmpty()) {
            Platform.runLater(() -> {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
                searchResultsVBox.getChildren().clear();
                searchResultsVBox.getChildren().addAll(emptyState, loadingIndicator);
            });
            return;
        }

        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            searchResultsVBox.getChildren().clear();
            searchResultsVBox.getChildren().add(loadingIndicator);
        });

        new Thread(() -> {
            try {
                String urlString = "http://localhost:8080/api/users/search?query=" + searchText;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());

                int responseCode = connection.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode != 200) {
                    Platform.runLater(() -> {
                        System.out.println("Failed to fetch users: HTTP " + responseCode);
                        loadingIndicator.setVisible(false);
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                        searchResultsVBox.getChildren().clear();
                        searchResultsVBox.getChildren().addAll(emptyState, loadingIndicator);
                    });
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                System.out.println("Raw JSON Response: " + response);
                ObjectMapper objectMapper = new ObjectMapper();
                List<UserSearchDTO> users = objectMapper.readValue(response.toString(), new TypeReference<>() {
                });
                System.out.println("Parsed Users: " + users);

                Platform.runLater(() -> {
                    searchResultsVBox.getChildren().clear();
                    loadingIndicator.setVisible(false);
                    if (users.isEmpty()) {
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                        searchResultsVBox.getChildren().addAll(emptyState, loadingIndicator);
                    } else {
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        for (UserSearchDTO user : users) {
                            try {
                                System.out.println("Loading user: " + user.getDisplayName());
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/AddUserItem.fxml"));
                                HBox userItem = loader.load(); // Changed to HBox
                                AddUserItem controller = loader.getController();
                                controller.setUserData(user);
                                searchResultsVBox.getChildren().add(userItem);
                            } catch (IOException e) {
                                Platform.runLater(() -> ShowDialogs.showErrorDialog("Error loading user item: " + e.getMessage()));
                            }
                        }
                        searchResultsVBox.getChildren().add(loadingIndicator);
                    }
                });

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                Platform.runLater(() -> {
                    ShowDialogs.showErrorDialog("Error during API call: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                    emptyState.setVisible(true);
                    emptyState.setManaged(true);
                    searchResultsVBox.getChildren().clear();
                    searchResultsVBox.getChildren().addAll(emptyState, loadingIndicator);
                });
            }
        }).start();
    }

    @FXML
    public void navigateBack(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/chat_frontend/ChatApp.fxml", "Chat & talk 🐍");
    }
}