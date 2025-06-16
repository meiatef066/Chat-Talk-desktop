package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.APIService;
import com.example.chat_frontend.API.ApiClient;
import com.example.chat_frontend.API.ChatAppApi;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.SimpleUserDTO;
import com.example.chat_frontend.utils.TokenManager;
import com.example.chat_frontend.utils.Validation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ChatApp {
    private static final Logger logger = LoggerFactory.getLogger(ChatApp.class);

    @FXML
    private TextField searchFriendField;
    @FXML
    private Button addFriendButton;
    @FXML
    private TabPane contactTabs;
    @FXML
    private ListView<SimpleUserDTO> friendList;
    @FXML
    private ListView<ContactResponse> pendingRequestList;
    @FXML
    private VBox rightPane;
    @FXML
    private VBox chatWindow;
    @FXML
    private VBox addFriendPane;
    @FXML
    private ImageView friendAvatar;
    @FXML
    private Label friendNameLabel;
    @FXML
    private Label friendStatusLabel;
    @FXML
    private VBox messageList;
    @FXML
    private TextArea messageInput;
    @FXML
    private Button sendMessageButton;
    @FXML
    private TextField searchUserField;
    @FXML
    private ListView<SimpleUserDTO> userSearchResults;
    private ObservableList<SimpleUserDTO> friends = FXCollections.observableArrayList();
    private ObservableList<ContactResponse> pendingRequests = FXCollections.observableArrayList();
    private ObservableList<SimpleUserDTO> searchResults = FXCollections.observableArrayList();
    private ChatAppApi api = new ChatAppApi(new ApiClient());
    private final APIService apiService = new APIService();
    private StompSession stompSession;
    @FXML
    public void initialize() {
        // Debug token
        String token = TokenManager.getInstance().getToken();
        System.out.println("Token: " + token);
        if (!Validation.validateToken()) {
            logger.error("No token available, skipping contact fetch");
            return;
        }

        // Set up ListView with custom cells
        friendList.setItems(friends);
        friendList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem( SimpleUserDTO userDTO, boolean empty ) {
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
                        logger.error("Error loading user item: {}", e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });
        pendingRequestList.setItems(pendingRequests);
        pendingRequestList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem( ContactResponse contactResponse, boolean empty ) {
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
                        logger.error("Error loading user item: {}", e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });
        // Fetch accepted contacts
        // Fetch contacts asynchronously
        Task<Void> fetchContactsTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    JsonNode jsonNode = api.getAllAcceptedUser();
                    JsonNode pending = api.getPendingRequests();
                    ObjectMapper mapper = new ObjectMapper();
                    List<SimpleUserDTO> friendList = new ArrayList<>();
                    List<ContactResponse> pendingList = new ArrayList<>();

                    for (JsonNode node : jsonNode) {
                        JsonNode contactNode = node.get("contact");
                        SimpleUserDTO userDTO = mapper.convertValue(contactNode, SimpleUserDTO.class);
                        friendList.add(userDTO);
                    }
                    for (JsonNode node : pending) {
                        ContactResponse contactResponse = mapper.convertValue(node, ContactResponse.class);
                        pendingList.add(contactResponse);
                    }

                    Platform.runLater(() -> {
                        friends.clear();
                        pendingRequests.clear();
                        friends.addAll(friendList);
                        pendingRequests.addAll(pendingList);
                        logger.info("Loaded {} contacts", friends.size());
                        logger.info("Loaded {} pending requests", pendingRequests.size());
                    });
                } catch (IOException | InterruptedException e) {
                    logger.error("Error fetching contacts: {}", e.getMessage());
                }
                return null;
            }
        };
        new Thread(fetchContactsTask).start();
        setupWebSocket();
    }

    private void setupWebSocket() {
        try {
            WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            URI uri = URI.create("ws://localhost:8080/ws"); // ✅ Use URI instead of String
            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();

            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("Authorization", "Bearer " + TokenManager.getInstance().getToken());

            stompClient.connect(uri, webSocketHttpHeaders, connectHeaders, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    logger.info("WebSocket connected");
                    stompSession = session;

                    // ✅ Subscribe only if pendingRequests is not empty
                    Platform.runLater(() -> {
                        if (!pendingRequests.isEmpty()) {
                            String userEmail = pendingRequests.getFirst().getUser().getEmail();
                            session.subscribe("/user/" + userEmail + "/topic/friend-request-response", new StompFrameHandler() {
                                @Override
                                public Type getPayloadType(StompHeaders headers) {
                                    return ContactResponse.class;
                                }

                                @Override
                                public void handleFrame(StompHeaders headers, Object payload) {
                                    ContactResponse contactResponse = (ContactResponse) payload;
                                    Platform.runLater(() -> {
                                        if ("ACCEPTED".equals(contactResponse.getStatus())) {
                                            if (friends.stream().noneMatch(f -> f.getEmail().equals(contactResponse.getContact().getEmail()))) {
                                                friends.add(contactResponse.getContact());
                                            }
                                            pendingRequests.removeIf(req -> req.getContact().getEmail().equals(contactResponse.getContact().getEmail()));
                                            logger.info("Added to friends: {}", contactResponse.getContact().getEmail());
                                        } else if ("REJECTED".equals(contactResponse.getStatus())) {
                                            pendingRequests.removeIf(req -> req.getContact().getEmail().equals(contactResponse.getContact().getEmail()));
                                            logger.info("Removed from pending: {}", contactResponse.getContact().getEmail());
                                        }
                                    });
                                }
                            });
                        } else {
                            logger.warn("No pending requests to subscribe to.");
                        }
                    });
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    logger.error("WebSocket error: {}", exception.getMessage());
                    reconnectWebSocket();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("WebSocket transport error: {}", exception.getMessage());
                    reconnectWebSocket();
                }
            });

        } catch (Exception e) {
            logger.error("Error setting up WebSocket: {}", e.getMessage());
            reconnectWebSocket();
        }
    }

    private void reconnectWebSocket() {
        logger.info("Attempting to reconnect WebSocket in 5 seconds...");
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                setupWebSocket();
            } catch (InterruptedException e) {
                logger.error("Reconnect interrupted: {}", e.getMessage());
            }
        }).start();
    }
    public void filterFriends(KeyEvent keyEvent) {
    }

    public void showAddFriendPane(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
    }

    public void loadMessages(MouseEvent event) {
    }

    public void hideAddFriendPane(ActionEvent actionEvent) {
    }

    public void searchUsers(KeyEvent keyEvent) {
    }
}