package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.MessageDTO;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.Notifications.NotificationHandler;
import com.example.chat_frontend.Notifications.PopupNotificationManager;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.example.chat_frontend.websocket.WebSocketClientManager;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    private final ChatAppApi api = new ChatAppApi(this);
    private WebSocketClientManager webSocketClientManager;
    private final NotificationHandler notificationHandler = new PopupNotificationManager();
    private ObjectMapper mapper =new ObjectMapper();
    private String selectedFriendEmail;
    private Long currentChatId = null;

    @FXML
    public void initialize() {
        String token = TokenManager.getInstance().getToken();
        String email = TokenManager.getInstance().getEmail();

        if (!Validation.validateToken()) {
            logger.error("No token available, skipping contact fetch");
            notificationHandler.displayMessageNotification("Error", "Please log in to continue.");
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

        userSearchResults.setItems(searchResults);
        userSearchResults.setCellFactory(_ -> createSimpleUserCell());
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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chat_frontend/ChatUserItem.fxml"));
                        HBox cell = loader.load();
                        ChatAppUserItem controller = loader.getController();
                        controller.setUserData(userDTO, ChatApp.this);
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
                    List<ContactResponse> acceptedContacts = api.getAllAcceptedUser();
                    List<ContactResponse> pending = api.getPendingRequests();

                    List<SimpleUserDTO> friendList = new ArrayList<>();
                    for (ContactResponse node : acceptedContacts) {
                        friendList.add(mapper.convertValue(node.get("contact"), SimpleUserDTO.class));
                    }

                    List<ContactResponse> pendingList = new ArrayList<>();
                    for (ContactResponse node : pending) {
                        pendingList.add(mapper.convertValue(node, ContactResponse.class));
                    }

                    Platform.runLater(() -> {
                        friends.setAll(friendList);
                        pendingRequests.setAll(pendingList);
                    });
                } catch (IOException | InterruptedException e) {
                    logger.error("Error fetching contacts: {}", e.getMessage(), e);
                    Platform.runLater(() -> notificationHandler.displayMessageNotification("Error", "Failed to load contacts: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void handleFriendResponse(ContactResponse response) {
        Platform.runLater(() -> {
            String contactEmail = response.getContact().getEmail();
            if ("ACCEPTED".equalsIgnoreCase(response.getStatus())) {
                if (friends.stream().noneMatch(f -> f.getEmail().equals(contactEmail))) {
                    friends.add(response.getContact());
                }
            }
            pendingRequests.removeIf(req -> req.getContact().getEmail().equals(contactEmail));
        });
    }

    public void addIncomingRequest(ContactResponse response) {
        Platform.runLater(() -> {
            if (response == null || response.getContact() == null) {
                logger.error("Invalid ContactResponse received: {}", response);
                notificationHandler.displayErrorNotification("Error", "Received invalid friend request data.");
                return;
            }
            boolean alreadyExists = pendingRequests.stream()
                    .anyMatch(req -> req.getContact() != null &&
                            req.getContact().getEmail().equals(response.getContact().getEmail()));
            if (!alreadyExists) {
                pendingRequests.add(response);
            }
        });
    }

    public void handleIncomingMessage(MessageDTO message) {
        Platform.runLater(() -> {
            if (currentChatId != null && currentChatId.equals(message.getChatId())) {
                addMessageToChatUI(message);
            }
        });
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) throws IOException, InterruptedException {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            notificationHandler.displayErrorNotification("Warning", "Message cannot be empty");
            return;
        }

        if (selectedFriendEmail == null) {
            notificationHandler.displayErrorNotification("Error", "No friend selected.");
            return;
        }

        if (currentChatId == null) {
            currentChatId = getChatId(); // Cache chatId
        }

        MessageDTO msg = new MessageDTO(
                currentChatId,
                TokenManager.getInstance().getEmail(),
                message,
                Instant.now().toString(),
                false
        );

        String destination = "/app/chat.send";
        webSocketClientManager.sendMessage(destination, msg);
        addMessageToChatUI(msg);
        messageInput.clear();
    }

    private Long getChatId() throws IOException, InterruptedException {
//        Task
        return api.getOrCreatePrivateChat(selectedFriendEmail, TokenManager.getInstance().getEmail()).getId();
    }

    public void onFriendSelected(SimpleUserDTO friend) {
        selectedFriendEmail = friend.getEmail();
        friendNameLabel.setText(friend.getFirstName() + " " + friend.getLastName());
        messageList.getChildren().clear();
        currentChatId = null; // reset to refetch

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                currentChatId = getChatId();
                List<MessageDTO> messages = api.getChatMessages(currentChatId);

                Platform.runLater(() -> {
                    for (MessageDTO msg : messages) {
                        addMessageToChatUI(msg);
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void addMessageToChatUI(MessageDTO msg) {
        Label label = new Label(msg.getSenderEmail() + ": " + msg.getContent());
        label.setWrapText(true);
        label.setStyle(msg.getSenderEmail().equals(TokenManager.getInstance().getEmail())
                ? "-fx-background-color: #cce5ff; -fx-padding: 8px; -fx-background-radius: 8px;"
                : "-fx-background-color: #f1f0f0; -fx-padding: 8px; -fx-background-radius: 8px;");
        messageList.getChildren().add(label);
    }

    @FXML
    public void filterFriends(KeyEvent keyEvent) {
        String searchText = searchFriendField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            friendList.setItems(friends);
        } else {
            ObservableList<SimpleUserDTO> filtered = FXCollections.observableArrayList();
            for (SimpleUserDTO friend : friends) {
                String fullName = (friend.getFirstName() + " " + friend.getLastName()).toLowerCase();
                if (fullName.contains(searchText)) {
                    filtered.add(friend);
                }
            }
            friendList.setItems(filtered);
        }
    }

    @FXML
    public void showAddFriendPane(ActionEvent actionEvent) {
        rightPane.getChildren().clear();
        rightPane.getChildren().add(addFriendPane);
        searchUserField.clear();
        searchResults.clear();
    }

    @FXML
    public void hideAddFriendPane(ActionEvent actionEvent) {
        rightPane.getChildren().clear();
        rightPane.getChildren().add(chatWindow);
    }

    @FXML
    public void loadMessages(MouseEvent event) {
        SimpleUserDTO selectedFriend = friendList.getSelectionModel().getSelectedItem();
        if (selectedFriend != null) {
            onFriendSelected(selectedFriend);
        }
    }

    @FXML
    public void searchUsers(KeyEvent keyEvent) {
        String searchText = searchUserField.getText().trim();
        if (searchText.isEmpty()) {
            searchResults.clear();
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    JsonNode node = api.searchUsers(searchText); // Expecting JsonNode from API
                    ObjectMapper mapper = new ObjectMapper();
                    List<SimpleUserDTO> results = mapper.convertValue(node, new TypeReference<List<SimpleUserDTO>>() {});
                    Platform.runLater(() -> searchResults.setAll(results));
                } catch (IllegalArgumentException e) {
                    logger.error("Error deserializing users: {}", e.getMessage(), e);
                    Platform.runLater(() ->
                            notificationHandler.displayMessageNotification("Error", "Failed to deserialize users: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}