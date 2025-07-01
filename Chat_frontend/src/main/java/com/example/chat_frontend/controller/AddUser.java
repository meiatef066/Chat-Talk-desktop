package com.example.chat_frontend.controller;

import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.Notifications.NotificationHandler;
import com.example.chat_frontend.Notifications.PopupNotificationManager;
import com.example.chat_frontend.utils.NavigationUtil;
import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private final NotificationHandler notificationHandler = new PopupNotificationManager();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8080";

    @FXML
    public void initialize() {
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        loadingIndicator.setVisible(false);
        searchResultsVBox.getChildren().clear();


    }

    @FXML
    public void searchUsers(KeyEvent keyEvent) {
        String searchText = contactSearchField.getText().trim();

        if (searchText.isEmpty()) {
            updateUIForEmptySearch();
            return;
        }

        showLoadingState();

        Task<List<ContactResponse>> fetchTask = new Task<>() {
            @Override
            protected List<ContactResponse> call() throws Exception {
                return fetchUsers(searchText);
            }

            @Override
            protected void succeeded() {
                updateUIWithResults(getValue());
            }

            @Override
            protected void failed() {
                notificationHandler.displayMessageNotification("Search Failed", "Could not fetch users. Please check your connection.");
                updateUIForEmptySearch();
            }
        };

        new Thread(fetchTask).start();
    }

    private void updateUIForEmptySearch() {
        Platform.runLater(() -> {
            searchResultsVBox.getChildren().clear();
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            loadingIndicator.setVisible(false);
        });
    }

    private void showLoadingState() {
        Platform.runLater(() -> {
            searchResultsVBox.getChildren().clear();
            loadingIndicator.setVisible(true);
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            searchResultsVBox.getChildren().add(loadingIndicator);
        });
    }

    private List<ContactResponse> fetchUsers(String searchText) throws IOException {
        String encodedQuery = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        String urlString = BASE_URL + "/api/users/search?query=" + encodedQuery;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Failed to fetch users: HTTP " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return objectMapper.readValue(response.toString(), new TypeReference<>() {});
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateUIWithResults(List<ContactResponse> users) {
        Platform.runLater(() -> {
            searchResultsVBox.getChildren().clear();
            loadingIndicator.setVisible(false);

            if (users.isEmpty()) {
                updateUIForEmptySearch();
                return;
            }

            emptyState.setVisible(false);
            emptyState.setManaged(false);

            for (ContactResponse user : users) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/AddUserItem.fxml"));
                    HBox userItem = loader.load();
                    AddUserItem controller = loader.getController();
                    controller.setUserData(user);
                    searchResultsVBox.getChildren().add(userItem);
                } catch (IOException e) {
                    notificationHandler.displayMessageNotification("UI Error", "Could not load user component.");
                }
            }
        });
    }

    @FXML
    public void navigateBack(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/chat_frontend/ChatApp.fxml", "Chat & talk 🐍");
    }
}
