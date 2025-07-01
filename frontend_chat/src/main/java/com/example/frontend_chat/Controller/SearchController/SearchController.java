package com.example.frontend_chat.Controller.SearchController;

import com.example.frontend_chat.API.SearchApi.SearchAPI;
import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.NotificationService.NotificationPopup;
import com.example.frontend_chat.utils.NavigationUtil;
import com.example.frontend_chat.utils.TokenManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
@Setter
public class SearchController {
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
    private final SearchAPI searchAPI=new SearchAPI();
    private final NotificationPopup notificationPopup = new NotificationPopup();
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
    public void searchUsers( KeyEvent keyEvent) {
        String searchText = contactSearchField.getText().trim();

        if (searchText.isEmpty()) {
            updateUIForEmptySearch();
        } else {
            searchUsersAsync(searchText);
        }
    }
    private void searchUsersAsync(String searchText) {
        showLoadingState();

        Task<List<ContactResponse>> fetchTask = new Task<>() {
            @Override
            protected List<ContactResponse> call() throws Exception {
                return searchAPI.searchUsers(searchText);  // now uses SearchAPI
            }

            @Override
            protected void succeeded() {
                updateUIWithResults(getValue());
            }

            @Override
            protected void failed() {
                notificationPopup.displayMessageNotification("Search Failed", "Could not fetch users. Please check your connection.");
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend_chat/SearchPage/AddUserItem.fxml"));
                    HBox userItem = loader.load();
                    AddUserItem controller = loader.getController();
                    controller.setUserData(user,SearchController.this);
                    searchResultsVBox.getChildren().add(userItem);
                } catch (IOException e) {
                    notificationPopup.displayMessageNotification("UI Error", "Could not load user component.");
                }
            }
        });
    }

    @FXML
    public void navigateBack(ActionEvent actionEvent) {
        NavigationUtil.switchScene(actionEvent, "/com/example/frontend_chat/MainPage/ChatApp.fxml", "Chat & talk 🐍");
    }
}
