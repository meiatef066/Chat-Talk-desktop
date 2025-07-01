package com.example.frontend_chat.Controller.MainPageController;

import com.example.frontend_chat.API.MainPageAPI.ChatAppApi;
import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.DTO.NotificationPayload;
import com.example.frontend_chat.DTO.SimpleUserDTO;
import com.example.frontend_chat.NotificationService.NotificationPopup;
import com.example.frontend_chat.WebSocket.GeneralWebSocketClient;
import com.example.frontend_chat.utils.TokenManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@Getter
@Setter
public class FriendList {

    private static final Logger logger = LoggerFactory.getLogger(FriendList.class);

    @FXML
    public TabPane contactTabs;
    @FXML
    public ListView<ContactResponse> friendList;
    @FXML
    public ListView<ContactResponse> pendingRequestList;

    private ChatAppApi chatAppApi;
    private ObservableList<ContactResponse> friendObservableList;
    private ObservableList<ContactResponse> pendingObservableList;
    private ChatApp chatApp;
    private GeneralWebSocketClient wsClient;
    private NotificationPopup notificationPopup;
    public FriendList() {
        logger.info("Initializing FriendList instance");
        this.chatAppApi = new ChatAppApi();
        this.friendObservableList = FXCollections.observableArrayList();
        this.pendingObservableList = FXCollections.observableArrayList();
    }
    @FXML
    public void initialize() {
        notificationPopup = new NotificationPopup();
        logger.info("Initializing FriendList UI components");
        wsClient = new GeneralWebSocketClient(
                "ws://localhost:8080/ws",
                TokenManager.getInstance().getToken(),
                true,  // Use SockJS
                true   // Enable automatic reconnect
        );

// Subscribe to friend request
        wsClient.addSubscription("/user/queue/friend-request", new StompFrameHandler() {
            @Override
            public Type getPayloadType( StompHeaders headers) {
                return NotificationPayload.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("🔔 Friend Request: " + payload.toString());
                if (payload instanceof NotificationPayload notification) {

                    String msg = notification.getMessage(); // "tara sent you a friend request"

                    System.out.println("🔔 Friend Request from Notification: " + msg);

                    notificationPopup.displayMessageNotification(String.valueOf(notification.getType()), msg);

                    // Optional: Extract more details if needed
                    fetchPendingRequestsAsync();
                    System.out.println("🔧 Full Data: " + notification.getData());
                } else {
                    System.out.println("⚠️ Unexpected payload type: " + payload);
                }
            }
        });

// Subscribe to messages
        wsClient.addSubscription("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            @Override
            public void handleFrame( StompHeaders headers, Object payload) {
                System.out.println("💬 Message: " + payload);
            }
        });

        wsClient.connect();

        configureLists();
        fetchFriendListAsync();
        fetchPendingRequestsAsync();
    }

    private void configureLists() {
        logger.debug("Configuring friend and pending request lists");
        friendList.setItems(friendObservableList);
        friendList.setCellFactory(param -> new ListCell<ContactResponse>() {
            private Node graphicNode;
            private ChatUserItem controller;

            {
                try {
                    logger.debug("Loading ChatUserItem.fxml for friend list cell");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend_chat/MainPage/ChatUserItem.fxml"));
                    graphicNode = loader.load();
                    controller = loader.getController();
                    logger.debug("Successfully loaded ChatUserItem.fxml");
                } catch (IOException e) {
                    logger.error("Failed to load ChatUserItem.fxml", e);
                    throw new RuntimeException("Failed to load ChatUserItem.fxml", e);
                }
            }

            @Override
            protected void updateItem( ContactResponse contact, boolean empty ) {
                super.updateItem(contact, empty);
                if (empty || contact == null || contact.getContact() == null) {
                    setGraphic(null);
                    logger.warn("Invalid or empty contact data: {}", contact);
                    return;
                }
                try {
                    logger.debug("Updating friend item for contact: {}", contact.getContact().getEmail());
                    controller.setUserData(contact.getContact(), FriendList.this);
                    setGraphic(graphicNode);
                } catch (Exception e) {
                    logger.error("Error updating friend item for contact: {}", contact.getContact().getEmail(), e);
                    setText("Error updating friend item");
                }
            }
        });

        pendingRequestList.setItems(pendingObservableList);
        pendingRequestList.setCellFactory(param -> new ListCell<ContactResponse>() {
            private Node graphicNode;
            private PendingRequestItem controller;

            {
                try {
                    logger.debug("Loading PendingRequestItem.fxml for pending request list cell");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend_chat/MainPage/PendingRequestItem.fxml"));
                    graphicNode = loader.load();
                    controller = loader.getController();
                    logger.debug("Successfully loaded PendingRequestItem.fxml");
                } catch (IOException e) {
                    logger.error("Failed to load PendingRequestItem.fxml", e);
                    throw new RuntimeException("Failed to load PendingRequestItem.fxml", e);
                }
            }

            @Override
            protected void updateItem( ContactResponse contact, boolean empty ) {
                super.updateItem(contact, empty);
                if (empty || contact == null || contact.getContact() == null) {
                    setGraphic(null);
                    logger.warn("Invalid or empty contact data: {}", contact);
                    return;
                }
                try {
                    logger.debug("Updating pending request item for contact: {}", contact.getContact().getEmail());
                    controller.setData(contact, FriendList.this);
                    setGraphic(graphicNode);
                } catch (Exception e) {
                    logger.error("Error updating pending request item for contact: {}", contact.getContact().getEmail(), e);
                    setText("Error updating pending request item");
                }
            }
        });
    }

    public void fetchFriendListAsync() {
        logger.info("Fetching accepted contacts asynchronously");
        Task<List<ContactResponse>> task = new Task<>() {
            @Override
            protected List<ContactResponse> call() throws Exception {
                ResponseEntity<List<ContactResponse>> response = chatAppApi.getAcceptedContacts();
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.debug("Successfully fetched accepted contacts: {}", response.getBody());
                    return response.getBody();
                } else {
                    logger.warn("Failed to fetch accepted contacts, status code: {}", response.getStatusCode());
                    throw new Exception("Failed to fetch friend list: " + response.getStatusCode());
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<ContactResponse> contacts = task.getValue();
            logger.debug("Updating friend list with {} contacts", contacts != null ? contacts.size() : 0);
            Platform.runLater(() -> {
                friendObservableList.clear();
                if (contacts != null) {
                    friendObservableList.addAll(contacts);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = task.getException();
                if (exception instanceof HttpClientErrorException && ((HttpClientErrorException) exception).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    logger.error("Authentication error while fetching friend list", exception);
                    showAlert("Authentication Error", "Invalid or expired token. Please log in again.");
                } else {
                    logger.error("Failed to fetch friend list", exception);
                    showAlert("Error", "Failed to fetch friend list: " + exception.getMessage());
                }
            });
        });

        new Thread(task).start();
    }

    public void fetchPendingRequestsAsync() {
        logger.info("Fetching pending contacts asynchronously");
        Task<List<ContactResponse>> task = new Task<>() {
            @Override
            protected List<ContactResponse> call() throws Exception {
                ResponseEntity<List<ContactResponse>> response = chatAppApi.getPendingContacts();
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.debug("Successfully fetched pending contacts: {}", response.getBody());
                    return response.getBody();
                } else {
                    logger.warn("Failed to fetch pending contacts, status code: {}", response.getStatusCode());
                    throw new Exception("Failed to fetch pending requests: " + response.getStatusCode());
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<ContactResponse> contacts = task.getValue();
            logger.debug("Updating pending request list with {} contacts", contacts != null ? contacts.size() : 0);
            Platform.runLater(() -> {
                pendingObservableList.clear();
                if (contacts != null) {
                    pendingObservableList.addAll(contacts);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = task.getException();
                if (exception instanceof HttpClientErrorException && ((HttpClientErrorException) exception).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    logger.error("Authentication error while fetching pending requests", exception);
                    showAlert("Authentication Error", "Invalid or expired token. Please log in again.");
                } else {
                    logger.error("Failed to fetch pending requests", exception);
                    showAlert("Error", "Failed to fetch pending requests: " + exception.getMessage());
                }
            });
        });

        new Thread(task).start();
    }

    public void loadMessagesForUser( SimpleUserDTO user ) {
        if (chatApp != null) {
            logger.info("Loading messages for user: {}", user.getEmail());
            System.out.println("Loading messages for user: " + user.getEmail());
            // TODO: Implement message loading logic with API call
        } else {
            logger.warn("ChatApp is null, cannot load messages for user: {}", user.getEmail());
        }
    }

    @FXML
    public void filterFriends( KeyEvent keyEvent ) {
        logger.debug("Filtering friends with key event: {}", keyEvent.getCode());
        // Implement search filtering logic if needed
    }

    @FXML
    public void showAddFriendPane( ActionEvent actionEvent ) {
        logger.info("Navigating to add friend pane");
        // Implement navigation to add friend pane
    }

    @FXML
    public void loadMessages( MouseEvent mouseEvent ) {
        logger.info("Loading messages triggered by mouse event");
        ContactResponse selectedContact = friendList.getSelectionModel().getSelectedItem();
        if (selectedContact != null) {
            logger.debug("Selected contact for messages: {}", selectedContact.getContact().getEmail());
            loadMessagesForUser(selectedContact.getContact());
        } else {
            logger.warn("No contact selected for loading messages");
        }
    }

    private void showAlert( String title, String message ) {
        logger.info("Showing alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}