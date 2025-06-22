//
//package com.example.chat_frontend.controller;
//
//import com.example.chat_frontend.API.ChatAppApi;
//import com.example.chat_frontend.DTO.ContactResponse;
//import com.example.chat_frontend.DTO.SimpleUserDTO;
//import com.example.chat_frontend.utils.TokenManager;
//import com.example.chat_frontend.utils.Validation;
//import com.example.chat_frontend.websocket.WebSocketClientManager;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PreDestroy;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.concurrent.Task;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.control.*;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import lombok.Getter;
//import lombok.Setter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//@Getter
//@Setter
//public class ChatApp {
//    private static final Logger logger = LoggerFactory.getLogger(ChatApp.class);
//
//    @FXML private TextField searchFriendField, searchUserField;
//    @FXML private Button addFriendButton, sendMessageButton;
//    @FXML private TabPane contactTabs;
//    @FXML private ListView<SimpleUserDTO> friendList, userSearchResults;
//    @FXML private ListView<ContactResponse> pendingRequestList;
//    @FXML private VBox rightPane, chatWindow, addFriendPane, messageList;
//    @FXML private ImageView friendAvatar;
//    @FXML private Label friendNameLabel, friendStatusLabel;
//    @FXML private TextArea messageInput;
//
//    private final ObservableList<SimpleUserDTO> friends = FXCollections.observableArrayList();
//    private final ObservableList<ContactResponse> pendingRequests = FXCollections.observableArrayList();
//    private final ObservableList<SimpleUserDTO> searchResults = FXCollections.observableArrayList();
//    private final ChatAppApi api = new ChatAppApi();
//    private WebSocketClientManager webSocketClientManager;
//
//    @FXML
//    public void initialize() {
//        String token = TokenManager.getInstance().getToken();
//        String email = TokenManager.getInstance().getEmail();
//        if (!Validation.validateToken()) {
//            logger.error("No token available, skipping contact fetch");
//            showAlert(Alert.AlertType.ERROR, "Please log in to continue.");
//            return;
//        }
//
//        webSocketClientManager = new WebSocketClientManager(this, token);
//        webSocketClientManager.connect();
//
//        configureListViews();
//        fetchContactsAsync();
//    }
//
//    @PreDestroy
//    public void onClose() {
//        if (webSocketClientManager != null) {
//            webSocketClientManager.disconnect();
//        }
//    }
//
//    private void configureListViews() {
//        friendList.setItems(friends);
//        friendList.setCellFactory(_ -> createSimpleUserCell());
//
//        pendingRequestList.setItems(pendingRequests);
//        pendingRequestList.setCellFactory(_ -> createPendingRequestCell());
//    }
//
//    private ListCell<SimpleUserDTO> createSimpleUserCell() {
//        return new ListCell<>() {
//            @Override
//            protected void updateItem(SimpleUserDTO userDTO, boolean empty) {
//                super.updateItem(userDTO, empty);
//                if (empty || userDTO == null) {
//                    setGraphic(null);
//                } else {
//                    try {
//                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/UserItem.fxml"));
//                        HBox cell = loader.load();
//                        UserItem controller = loader.getController();
//                        controller.setUserData(userDTO);
//                        setGraphic(cell);
//                    } catch (Exception e) {
//                        logger.error("Error loading user item: {}", e.getMessage());
//                        setGraphic(null);
//                    }
//                }
//            }
//        };
//    }
//
//    private ListCell<ContactResponse> createPendingRequestCell() {
//        return new ListCell<>() {
//            @Override
//            protected void updateItem(ContactResponse contactResponse, boolean empty) {
//                super.updateItem(contactResponse, empty);
//                if (empty || contactResponse == null) {
//                    setGraphic(null);
//                } else {
//                    try {
//                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/PendingRequestItem.fxml"));
//                        HBox cell = loader.load();
//                        PendingRequestItem controller = loader.getController();
//                        controller.setUserData(contactResponse);
//                        controller.setChatAppController(ChatApp.this);
//                        setGraphic(cell);
//                    } catch (Exception e) {
//                        logger.error("Error loading pending request item: {}", e.getMessage());
//                        setGraphic(null);
//                    }
//                }
//            }
//        };
//    }
//
//    private void fetchContactsAsync() {
//        Task<Void> task = new Task<>() {
//            @Override
//            protected Void call() {
//                try {
//                    JsonNode acceptedContacts = api.getAllAcceptedUser();
//                    JsonNode pending = api.getPendingRequests();
//                    ObjectMapper mapper = new ObjectMapper();
//
//                    List<SimpleUserDTO> friendList = new ArrayList<>();
//                    for (JsonNode node : acceptedContacts) {
//                        friendList.add(mapper.convertValue(node.get("contact"), SimpleUserDTO.class));
//                    }
//
//                    List<ContactResponse> pendingList = new ArrayList<>();
//                    for (JsonNode node : pending) {
//                        pendingList.add(mapper.convertValue(node, ContactResponse.class));
//                    }
//
//                    Platform.runLater(() -> {
//                        friends.setAll(friendList);
//                        pendingRequests.setAll(pendingList);
//                        logger.info("Loaded {} contacts", friends.size());
//                        logger.info("Loaded {} pending requests", pendingRequests.size());
//                    });
//                } catch (IOException | InterruptedException e) {
//                    logger.error("Error fetching contacts: {}", e.getMessage());
//                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to load contacts: " + e.getMessage()));
//                }
//                return null;
//            }
//        };
//        new Thread(task).start();
//    }
//
//    public void handleFriendResponse(ContactResponse response) {
//        String contactEmail = response.getContact().getEmail();
//        Platform.runLater(() -> {
//            if ("ACCEPTED".equalsIgnoreCase(response.getStatus())) {
//                if (friends.stream().noneMatch(f -> f.getEmail().equals(contactEmail))) {
//                    friends.add(response.getContact());
//                }
//            }
//            pendingRequests.removeIf(req -> req.getContact().getEmail().equals(contactEmail));
//            logger.info("Friend request update: {} - {}", response.getStatus(), contactEmail);
//        });
//    }
//
//    public void addIncomingRequest(ContactResponse response) {
//        Platform.runLater(() -> {
//            boolean alreadyExists = pendingRequests.stream()
//                    .anyMatch(req -> req.getContact().getEmail().equals(response.getContact().getEmail()));
//            if (!alreadyExists) {
//                pendingRequests.add(response);
//                logger.info("New pending request from {}", response.getContact().getEmail());
//            }
//        });
//    }
//
//    public void showAlert( Alert.AlertType type, String message ) {
//        Alert alert = new Alert(type);
//        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    // Placeholder methods for other functionalities
//    public void filterFriends(KeyEvent keyEvent) {}
//    public void showAddFriendPane(ActionEvent actionEvent) {}
//    public void hideAddFriendPane(ActionEvent actionEvent) {}
//    public void sendMessage(ActionEvent actionEvent) {}
//    public void loadMessages(MouseEvent event) {}
//    public void searchUsers(KeyEvent keyEvent) {}
//
//
//}
package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.example.chat_frontend.websocket.WebSocketClientManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the main chat application UI.
 */
@Getter
@Setter
public class ChatApp {
    private static final Logger logger = LoggerFactory.getLogger(ChatApp.class);

    @FXML private TextField searchFriendField, searchUserField;
    @FXML private Button addFriendButton, sendMessageButton;
    @FXML private TabPane contactTabs;
    @FXML private ListView<SimpleUserDTO> friendList, userSearchResults;
    @FXML private ListView<ContactResponse> pendingRequestList;
    @FXML private VBox rightPane, chatWindow, addFriendPane, messageList;
    @FXML private ImageView friendAvatar;
    @FXML private Label friendNameLabel, friendStatusLabel;
    @FXML private TextArea messageInput;

    private final ObservableList<SimpleUserDTO> friends = FXCollections.observableArrayList();
    private final ObservableList<ContactResponse> pendingRequests = FXCollections.observableArrayList();
    private final ObservableList<SimpleUserDTO> searchResults = FXCollections.observableArrayList();
    private final ChatAppApi api = new ChatAppApi();
    private WebSocketClientManager webSocketClientManager;

    @FXML
    public void initialize() {
        String token = TokenManager.getInstance().getToken();
        String email = TokenManager.getInstance().getEmail();
        if (!Validation.validateToken()) {
            logger.error("No token available, skipping contact fetch");
            showAlert(Alert.AlertType.ERROR, "Please log in to continue.");
            return;
        }

        logger.info("Initializing ChatApp for user: {}", email);
        webSocketClientManager = new WebSocketClientManager(this, token);
        webSocketClientManager.connect();

        configureListViews();
        fetchContactsAsync();
    }

    @PreDestroy
    public void onClose() {
        if (webSocketClientManager != null) {
            webSocketClientManager.disconnect();
            logger.info("WebSocket client disconnected on application close");
        }
    }

    private void configureListViews() {
        friendList.setItems(friends);
        friendList.setCellFactory(_ -> createSimpleUserCell());

        pendingRequestList.setItems(pendingRequests);
        pendingRequestList.setCellFactory(_ -> createPendingRequestCell());
    }

    private ListCell<SimpleUserDTO> createSimpleUserCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(SimpleUserDTO userDTO, boolean empty) {
                super.updateItem(userDTO, empty);
                if (empty || userDTO == null) {
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/UserItem.fxml"));
                        HBox cell = loader.load();
                        UserItem controller = loader.getController();
                        controller.setUserData(userDTO);
                        setGraphic(cell);
                    } catch (Exception e) {
                        logger.error("Error loading user item: {}", e.getMessage(), e);
                        setGraphic(null);
                    }
                }
            }
        };
    }

    private ListCell<ContactResponse> createPendingRequestCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ContactResponse contactResponse, boolean empty) {
                super.updateItem(contactResponse, empty);
                if (empty || contactResponse == null) {
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/PendingRequestItem.fxml"));
                        HBox cell = loader.load();
                        PendingRequestItem controller = loader.getController();
                        controller.setUserData(contactResponse);
                        controller.setChatAppController(ChatApp.this);
                        setGraphic(cell);
                    } catch (Exception e) {
                        logger.error("Error loading pending request item: {}", e.getMessage(), e);
                        setGraphic(null);
                    }
                }
            }
        };
    }

    private void fetchContactsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    JsonNode acceptedContacts = api.getAllAcceptedUser();
                    JsonNode pending = api.getPendingRequests();
                    ObjectMapper mapper = new ObjectMapper();

                    List<SimpleUserDTO> friendList = new ArrayList<>();
                    for (JsonNode node : acceptedContacts) {
                        friendList.add(mapper.convertValue(node.get("contact"), SimpleUserDTO.class));
                    }

                    List<ContactResponse> pendingList = new ArrayList<>();
                    for (JsonNode node : pending) {
                        pendingList.add(mapper.convertValue(node, ContactResponse.class));
                    }

                    Platform.runLater(() -> {
                        friends.setAll(friendList);
                        pendingRequests.setAll(pendingList);
                        logger.info("Loaded {} contacts", friends.size());
                        logger.info("Loaded {} pending requests", pendingRequests.size());
                    });
                } catch (IOException | InterruptedException e) {
                    logger.error("Error fetching contacts: {}", e.getMessage(), e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to load contacts: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void handleFriendResponse(ContactResponse response) {
        String contactEmail = response.getContact().getEmail();
        Platform.runLater(() -> {
            if ("ACCEPTED".equalsIgnoreCase(response.getStatus())) {
                if (friends.stream().noneMatch(f -> f.getEmail().equals(contactEmail))) {
                    friends.add(response.getContact());
                    logger.info("Added friend: {}", contactEmail);
                }
            }
            pendingRequests.removeIf(req -> req.getContact().getEmail().equals(contactEmail));
            logger.info("Friend request update: {} - {}", response.getStatus(), contactEmail);
        });
    }

    public void addIncomingRequest(ContactResponse response) {
        Platform.runLater(() -> {
            boolean alreadyExists = pendingRequests.stream()
                    .anyMatch(req -> req.getContact().getEmail().equals(response.getContact().getEmail()));
            if (!alreadyExists) {
                pendingRequests.add(response);
                logger.info("New pending request from {}", response.getContact().getEmail());
                showAlert(Alert.AlertType.INFORMATION, "New friend request from " + response.getContact().getEmail());
            }
        });
    }

    public void showAlert(Alert.AlertType type, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Message cannot be empty");
            return;
        }

        // Example: Send message to a specific destination (adjust based on backend)
        String destination = "/app/message"; // Backend should handle this destination
        webSocketClientManager.sendMessage(destination, new MessageDTO(message));
        messageInput.clear();
        logger.info("Sent message: {}", message);
    }

    // Placeholder methods for other functionalities
    public void filterFriends(KeyEvent keyEvent) {}
    public void showAddFriendPane(ActionEvent actionEvent) {}
    public void hideAddFriendPane(ActionEvent actionEvent) {}
    public void loadMessages(MouseEvent event) {}
    public void searchUsers(KeyEvent keyEvent) {}

    /**
     * DTO for sending messages via WebSocket.
     */
    public static class MessageDTO {
        private String content;

        public MessageDTO(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}