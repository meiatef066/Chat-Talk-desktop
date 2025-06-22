package com.example.chat_frontend.websocket;
//
//
//import javafx.application.Platform;
//import javafx.scene.control.Alert;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//
//public class WebSocketClientManager {
//    private WebSocketClient client;
//
//    // Replace with your actual topic, e.g., /user/topic/friend-request
//    private static final String WS_URL = "ws://localhost:8080/ws?token=";
//
//    public void connect(String token) {
//        try {
//            URI uri = new URI(WS_URL + token);
//
//            client = new WebSocketClient(uri) {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//                    System.out.println("✅ Connected to WebSocket");
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    System.out.println("📩 Received message: " + message);
//
//                    // Display real-time notification
//                    Platform.runLater(() -> {
//                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                        alert.setTitle("Friend Request Notification");
//                        alert.setHeaderText("You have a new friend request!");
//                        alert.setContentText(message);
//                        alert.showAndWait();
//                    });
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    System.out.println("❌ WebSocket Closed. Reason: " + reason);
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    System.err.println("⚠️ WebSocket Error: " + ex.getMessage());
//                }
//            };
//
//            client.connect();
//
//        } catch (URISyntaxException e) {
//            System.err.println("❌ Invalid WebSocket URI: " + e.getMessage());
//        }
//    }
//
//    public void disconnect() {
//        if (client != null && client.isOpen()) {
//            client.close();
//        }
//    }
//}

import com.example.chat_frontend.controller.ChatApp;
import com.example.chat_frontend.DTO.ContactResponse;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manages WebSocket connection using STOMP protocol with SockJS fallback for real-time chat functionality.
 */
//public class WebSocketClientManager {
//
//    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientManager.class);
//    private static final String WS_URL = "http://localhost:8080/ws";
//    private static final String FRIEND_REQUEST_TOPIC = "/user/topic/friend-request";
//    private static final String FRIEND_RESPONSE_TOPIC = "/user/topic/friend-request-response";
//    private static final long INITIAL_RECONNECT_DELAY = 5; // Seconds
//    private static final int MAX_RECONNECT_ATTEMPTS = 5;
//    private static final long CONNECT_TIMEOUT = 10; // Seconds
//
//    private final ChatApp chatApp;
//    private final String jwtToken;
//    private WebSocketStompClient stompClient;
//    private StompSession session;
//    private final Object lock = new Object();
//    private volatile boolean isRunning = false;
//    private int reconnectAttempts = 0;
//    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
//
//    public WebSocketClientManager(ChatApp chatApp, String jwtToken) {
//        this.chatApp = chatApp;
//        this.jwtToken = jwtToken;
//    }
//
//    public void connect() {
//        synchronized (lock) {
//            if (isRunning) {
//                logger.info("WebSocket client is already running");
//                return;
//            }
//            isRunning = true;
//            reconnectAttempts = 0;
//        }
//
//        try {
//            WebSocketClient webSocketClient = new StandardWebSocketClient();
//            SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(new WebSocketTransport(webSocketClient)));
//            stompClient = new WebSocketStompClient(sockJsClient);
//            stompClient.setDefaultHeartbeat(new long[]{0, 0});
//
//            StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
//                @Override
//                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//                    logger.info("Connected to WebSocket/SockJS server at {}", WS_URL);
//                    WebSocketClientManager.this.session = session;
//                    reconnectAttempts = 0;
//
//                    StompHeaders headers = new StompHeaders();
//                    headers.add("Authorization", "Bearer " + jwtToken);
//                    headers.add("token", jwtToken);
//
//                    session.subscribe(FRIEND_REQUEST_TOPIC, new StompFrameHandler() {
//                        @Override
//                        public Type getPayloadType(StompHeaders headers) {
//                            return ContactResponse.class;
//                        }
//
//                        @Override
//                        public void handleFrame(StompHeaders headers, Object payload) {
//                            logger.debug("Received message on {}: {}", FRIEND_REQUEST_TOPIC, payload);
//                            handleFriendRequest(payload);
//                        }
//                    });
//
//                    session.subscribe(FRIEND_RESPONSE_TOPIC, new StompFrameHandler() {
//                        @Override
//                        public Type getPayloadType(StompHeaders headers) {
//                            return ContactResponse.class;
//                        }
//
//                        @Override
//                        public void handleFrame(StompHeaders headers, Object payload) {
//                            logger.debug("Received message on {}: {}", FRIEND_RESPONSE_TOPIC, payload);
//                            handleFriendResponse(payload);
//                        }
//                    });
//
//                    logger.info("Subscribed to topics: {}, {}", FRIEND_REQUEST_TOPIC, FRIEND_RESPONSE_TOPIC);
//                    chatApp.showAlert(Alert.AlertType.INFORMATION, "Connected to WebSocket server");
//                }
//
//                @Override
//                public void handleException(StompSession session, StompCommand command, StompHeaders headers,
//                                            byte[] payload, Throwable exception) {
//                    logger.error("STOMP error on command {}: {}", command, exception.getMessage(), exception);
//                    chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket STOMP error: " + exception.getMessage());
//                    scheduleReconnect();
//                }
//
//                @Override
//                public void handleTransportError(StompSession session, Throwable exception) {
//                    logger.error("Transport error: {}", exception.getMessage(), exception);
//                    chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket transport error: " + exception.getMessage());
//                    scheduleReconnect();
//                }
//
//                @Override
//                public void handleFrame(StompHeaders headers, Object payload) {
//                    logger.debug("Received STOMP frame: headers={}, payload={}", headers, payload);
//                }
//            };
//
//            // Append token as query parameter and set headers for CONNECT
//            String urlWithToken = WS_URL + "?token=" + jwtToken;
//            logger.info("Attempting WebSocket/SockJS connection to {}", urlWithToken);
//            WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
//            httpHeaders.add("Authorization", "Bearer " + jwtToken);
//            StompHeaders connectHeaders = new StompHeaders();
//            connectHeaders.add("Authorization", "Bearer " + jwtToken);
//            connectHeaders.add("token", jwtToken);
//
//            // Use connect method returning ListenableFuture
//            ListenableFuture<StompSession> future = stompClient.connect(urlWithToken, httpHeaders, connectHeaders, sessionHandler);
//            session = future.get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
//            logger.info("WebSocket/SockJS connection established successfully");
//        } catch (TimeoutException e) {
//            logger.error("WebSocket connection timed out after {} seconds: {}", CONNECT_TIMEOUT, e.getMessage(), e);
//            chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket connection timed out: " + e.getMessage());
//            scheduleReconnect();
//        } catch (Exception e) {
//            logger.error("Unexpected error during WebSocket/SockJS connection setup: {}", e.getMessage(), e);
//            chatApp.showAlert(Alert.AlertType.ERROR, "Unexpected WebSocket error: " + e.getMessage());
//            scheduleReconnect();
//        }
//    }
//
//    public void disconnect() {
//        synchronized (lock) {
//            if (!isRunning) {
//                logger.info("WebSocket client is already disconnected");
//                return;
//            }
//            isRunning = false;
//        }
//
//        try {
//            scheduler.shutdown();
//            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
//                logger.warn("Scheduler did not terminate gracefully");
//                scheduler.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            logger.error("Error shutting down scheduler: {}", e.getMessage(), e);
//            Thread.currentThread().interrupt();
//        }
//
//        if (session != null && session.isConnected()) {
//            session.disconnect();
//            logger.info("Disconnected from WebSocket server");
//        }
//        if (stompClient != null) {
//            stompClient.stop();
//        }
//    }
//
//    public void sendMessage(String destination, Object payload) {
//        if (session != null && session.isConnected()) {
//            StompHeaders headers = new StompHeaders();
//            headers.setDestination(destination);
//            headers.add("Authorization", "Bearer " + jwtToken);
//            headers.add("token", jwtToken);
//            session.send(headers, payload);
//            logger.debug("Sent message to {}", destination);
//        } else {
//            logger.warn("Cannot send message: WebSocket is not connected");
//            chatApp.showAlert(Alert.AlertType.WARNING, "Cannot send message: WebSocket is not connected");
//        }
//    }
//
//    private void handleFriendRequest(Object payload) {
//        try {
//            if (payload instanceof ContactResponse response) {
//                chatApp.addIncomingRequest(response);
//                logger.info("Received friend request from {}", response.getContact().getEmail());
//            } else {
//                logger.warn("Unexpected payload type for friend request: {}", payload != null ? payload.getClass().getName() : "null");
//            }
//        } catch (Exception e) {
//            logger.error("Error processing friend request: {}", e.getMessage(), e);
//            chatApp.showAlert(Alert.AlertType.ERROR, "Error processing friend request: " + e.getMessage());
//        }
//    }
//
//    private void handleFriendResponse(Object payload) {
//        try {
//            if (payload instanceof ContactResponse response) {
//                chatApp.handleFriendResponse(response);
//                logger.info("Received friend request response: {} for {}", response.getStatus(), response.getContact().getEmail());
//            } else {
//                logger.warn("Unexpected payload type for friend response: {}", payload != null ? payload.getClass().getName() : "null");
//            }
//        } catch (Exception e) {
//            logger.error("Error processing friend response: {}", e.getMessage(), e);
//            chatApp.showAlert(Alert.AlertType.ERROR, "Error processing friend response: " + e.getMessage());
//        }
//    }
//
//    private void scheduleReconnect() {
//        synchronized (lock) {
//            if (!isRunning || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
//                logger.info("Max reconnect attempts ({}) reached or client stopped", MAX_RECONNECT_ATTEMPTS);
//                chatApp.showAlert(Alert.AlertType.ERROR, "Failed to reconnect to WebSocket after " + MAX_RECONNECT_ATTEMPTS + " attempts");
//                return;
//            }
//        }
//
//        reconnectAttempts++;
//        long delay = INITIAL_RECONNECT_DELAY * (long) Math.pow(2, reconnectAttempts - 1);
//        logger.info("Scheduling WebSocket reconnect attempt {} in {} seconds", reconnectAttempts, delay);
//        scheduler.schedule(() -> {
//            if (isRunning) {
//                disconnect();
//                connect();
//            }
//        }, delay, TimeUnit.SECONDS);
//    }
//}


import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.controller.ChatApp;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebSocketClientManager {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientManager.class);
    private static final String WS_URL = "http://localhost:8080/ws";
    private static final String FRIEND_REQUEST_TOPIC = "/user/topic/friend-request";
    private static final String FRIEND_RESPONSE_TOPIC = "/user/topic/friend-request-response";
    private static final long INITIAL_RECONNECT_DELAY = 5; // Seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long CONNECT_TIMEOUT = 10; // Seconds

    private final ChatApp chatApp;
    private final String jwtToken;
    private WebSocketStompClient stompClient;
    private StompSession session;
    private final Object lock = new Object();
    private volatile boolean isRunning = false;
    private int reconnectAttempts = 0;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

    public WebSocketClientManager(ChatApp chatApp, String jwtToken) {
        this.chatApp = chatApp;
        this.jwtToken = jwtToken;
    }

    public void connect() {
        synchronized (lock) {
            if (isRunning) {
                logger.info("WebSocket client is already running");
                return;
            }
            isRunning = true;
            reconnectAttempts = 0;
        }

        try {
            WebSocketClient webSocketClient = new StandardWebSocketClient();
            SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(new WebSocketTransport(webSocketClient)));
            stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setDefaultHeartbeat(new long[]{0, 0});
            // Add Jackson message converter
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    logger.info("Connected to WebSocket/SockJS server at {}", WS_URL);
                    WebSocketClientManager.this.session = session;
                    reconnectAttempts = 0;

                    StompHeaders headers = new StompHeaders();
                    headers.add("Authorization", "Bearer " + jwtToken);
                    headers.add("token", jwtToken);

                    session.subscribe(FRIEND_REQUEST_TOPIC, new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ContactResponse.class; // com.example.chat_frontend.DTO.ContactResponse
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            logger.debug("Received message on {}: {}", FRIEND_REQUEST_TOPIC, payload);
                            handleFriendRequest(payload);
                        }
                    });

                    session.subscribe(FRIEND_RESPONSE_TOPIC, new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ContactResponse.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            logger.debug("Received message on {}: {}", FRIEND_RESPONSE_TOPIC, payload);
                            handleFriendResponse(payload);
                        }
                    });

                    logger.info("Subscribed to topics: {}, {}", FRIEND_REQUEST_TOPIC, FRIEND_RESPONSE_TOPIC);
                    chatApp.showAlert(Alert.AlertType.INFORMATION, "Connected to WebSocket server");
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers,
                                            byte[] payload, Throwable exception) {
                    logger.error("STOMP error on command {}: {}", command, exception.getMessage(), exception);
                    chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket STOMP error: " + exception.getMessage());
                    scheduleReconnect();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("Transport error: {}", exception.getMessage(), exception);
                    chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket transport error: " + exception.getMessage());
                    scheduleReconnect();
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    logger.debug("Received STOMP frame: headers={}, payload={}", headers, payload);
                }
            };

            String urlWithToken = WS_URL + "?token=" + jwtToken;
            logger.info("Attempting WebSocket/SockJS connection to {}", urlWithToken);
            WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
            httpHeaders.add("Authorization", "Bearer " + jwtToken);
            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("Authorization", "Bearer " + jwtToken);
            connectHeaders.add("token", jwtToken);

            ListenableFuture<StompSession> future = stompClient.connect(urlWithToken, httpHeaders, connectHeaders, sessionHandler);
            session = future.get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            logger.info("WebSocket/SockJS connection established successfully");
        } catch (TimeoutException e) {
            logger.error("WebSocket connection timed out after {} seconds: {}", CONNECT_TIMEOUT, e.getMessage(), e);
            chatApp.showAlert(Alert.AlertType.ERROR, "WebSocket connection timed out: " + e.getMessage());
            scheduleReconnect();
        } catch (Exception e) {
            logger.error("Unexpected error during WebSocket/SockJS connection setup: {}", e.getMessage(), e);
            chatApp.showAlert(Alert.AlertType.ERROR, "Unexpected WebSocket error: " + e.getMessage());
            scheduleReconnect();
        }
    }

    public void disconnect() {
        synchronized (lock) {
            if (!isRunning) {
                logger.info("WebSocket client is already disconnected");
                return;
            }
            isRunning = false;
        }

        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Scheduler did not terminate gracefully");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error shutting down scheduler: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
            logger.info("Disconnected from WebSocket server");
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    public void sendMessage(String destination, Object payload) {
        if (session != null && session.isConnected()) {
            StompHeaders headers = new StompHeaders();
            headers.setDestination(destination);
            headers.add("Authorization", "Bearer " + jwtToken);
            headers.add("token", jwtToken);
            session.send(headers, payload);
            logger.debug("Sent message to {}", destination);
        } else {
            logger.warn("Cannot send message: WebSocket is not connected");
            chatApp.showAlert(Alert.AlertType.WARNING, "Cannot send message: WebSocket is not connected");
        }
    }

    private void handleFriendRequest(Object payload) {
        try {
            logger.debug("Raw payload: {}", payload);
            if (payload instanceof ContactResponse response) {
                chatApp.addIncomingRequest(response);
                logger.info("Received friend request from {}", response.getContact().getEmail());
            } else {
                logger.warn("Unexpected payload type for friend request: {}", payload != null ? payload.getClass().getName() : "null");
            }
        } catch (Exception e) {
            logger.error("Error processing friend request: {}", e.getMessage(), e);
            chatApp.showAlert(Alert.AlertType.ERROR, "Error processing friend request: " + e.getMessage());
        }
    }

    private void handleFriendResponse(Object payload) {
        try {
            logger.debug("Raw payload: {}", payload);
            if (payload instanceof ContactResponse response) {
                chatApp.handleFriendResponse(response);
                logger.info("Received friend request response: {} for {}", response.getStatus(), response.getContact().getEmail());
            } else {
                logger.warn("Unexpected payload type for friend response: {}", payload != null ? payload.getClass().getName() : "null");
            }
        } catch (Exception e) {
            logger.error("Error processing friend response: {}", e.getMessage(), e);
            chatApp.showAlert(Alert.AlertType.ERROR, "Error processing friend response: " + e.getMessage());
        }
    }

    private void scheduleReconnect() {
        synchronized (lock) {
            if (!isRunning || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                logger.info("Max reconnect attempts ({}) reached or client stopped", MAX_RECONNECT_ATTEMPTS);
                chatApp.showAlert(Alert.AlertType.ERROR, "Failed to reconnect to WebSocket after " + MAX_RECONNECT_ATTEMPTS + " attempts");
                return;
            }
        }

        reconnectAttempts++;
        long delay = INITIAL_RECONNECT_DELAY * (long) Math.pow(2, reconnectAttempts - 1);
        logger.info("Scheduling WebSocket reconnect attempt {} in {} seconds", reconnectAttempts, delay);
        scheduler.schedule(() -> {
            if (isRunning) {
                disconnect();
                connect();
            }
        }, delay, TimeUnit.SECONDS);
    }
}