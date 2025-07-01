package com.example.chat_frontend.websocket;

import com.example.chat_frontend.DTO.MessageDTO;
import com.example.chat_frontend.Notifications.PopupNotificationManager;
import com.example.chat_frontend.controller.ChatApp;
import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.Notifications.NotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketClientManager {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientManager.class);
    private static final String WS_URL = "ws://localhost:8080/ws";
    private static final String FRIEND_REQUEST_TOPIC = "/user/queue/friend-request";
    private static final String FRIEND_RESPONSE_TOPIC = "/user/queue/friend-request-response";
    private static final String MESSAGE_TOPIC = "/user/queue/messages";
    private static final long INITIAL_RECONNECT_DELAY = 5;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long CONNECT_TIMEOUT = 10;

    private final ChatApp chatApp;
    private final NotificationHandler notificationHandler;
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
        this.notificationHandler = new PopupNotificationManager();
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
            SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient())));
            stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            stompClient.setDefaultHeartbeat(new long[]{0, 0});

            StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    logger.info("Connected to WebSocket server at {}", WS_URL);
                    WebSocketClientManager.this.session = session;
                    reconnectAttempts = 0;

                    subscribeToTopics(session);
                    notificationHandler.displayMessageNotification("System", "Connected to WebSocket server");
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    logger.error("STOMP error on command {}: {}", command, exception.getMessage(), exception);
                    notificationHandler.displayMessageNotification("Error", "WebSocket STOMP error: " + exception.getMessage());
                    scheduleReconnect();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("Transport error: {}", exception.getMessage(), exception);
                    notificationHandler.displayMessageNotification("Error", "WebSocket transport error: " + exception.getMessage());
                    scheduleReconnect();
                }
            };

            String urlWithToken = WS_URL + "?token=" + jwtToken;
            logger.info("Connecting to {}", urlWithToken);
            WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
            httpHeaders.add("Authorization", "Bearer " + jwtToken);
            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("Authorization", "Bearer " + jwtToken);

            session = stompClient.connect(urlWithToken, httpHeaders, connectHeaders, sessionHandler)
                    .get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            logger.info("WebSocket connection established");
        } catch (Exception e) {
            logger.error("WebSocket connection failed: {}", e.getMessage(), e);
            notificationHandler.displayMessageNotification("Error", "WebSocket connection failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private void subscribeToTopics(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);

        session.subscribe(FRIEND_REQUEST_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ContactResponse.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                handlePayload(payload, FRIEND_REQUEST_TOPIC, response ->
                        notificationHandler.displayFriendRequestNotification(response.getContact().getEmail()));
            }
        });

        session.subscribe(FRIEND_RESPONSE_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ContactResponse.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                handlePayload(payload, FRIEND_RESPONSE_TOPIC, response ->
                        notificationHandler.displayFriendResponseNotification(response.getContact().getEmail(), response.getStatus()));
            }
        });

        session.subscribe(MESSAGE_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof MessageDTO message) {
                    chatApp.handleIncomingMessage(message);
                    notificationHandler.displayMessageNotification(message.getSenderEmail(), message.getContent());
                } else {
                    logger.warn("Unexpected message payload type: {}", payload != null ? payload.getClass().getName() : "null");
                }
            }
        });

        logger.info("Subscribed to topics: {}, {}, {}", FRIEND_REQUEST_TOPIC, FRIEND_RESPONSE_TOPIC, MESSAGE_TOPIC);
    }

    private void handlePayload(Object payload, String topic, java.util.function.Consumer<ContactResponse> notificationAction) {
        try {
            if (payload instanceof ContactResponse response) {
                if (response.getContact() == null) {
                    logger.error("ContactResponse has null contact for topic {}", topic);
                    notificationHandler.displayErrorNotification("Error", "Received invalid friend request data (missing contact)");
                    return;
                }
                if (topic.equals(FRIEND_REQUEST_TOPIC)) {
                    chatApp.addIncomingRequest(response);
                } else if (topic.equals(FRIEND_RESPONSE_TOPIC)) {
                    chatApp.handleFriendResponse(response);
                }
                notificationAction.accept(response);
                logger.info("Processed {} from {}", topic, response.getContact().getEmail());
            } else {
                logger.warn("Unexpected payload type for {}: {}", topic, payload != null ? payload.getClass().getName() : "null");
                notificationHandler.displayMessageNotification("Error", "Invalid payload received on " + topic);
            }
        } catch (Exception e) {
            logger.error("Error processing {}: {}", topic, e.getMessage(), e);
            notificationHandler.displayMessageNotification("Error", "Error processing " + topic + ": " + e.getMessage());
        }
    }

    public void sendMessage(String destination, Object payload) {
        if (session != null && session.isConnected()) {
            StompHeaders headers = new StompHeaders();
            headers.setDestination(destination);
            headers.add("Authorization", "Bearer " + jwtToken);
            session.send(headers, payload);
            logger.debug("Sent message to {}", destination);
        } else {
            logger.warn("Cannot send message: WebSocket is not connected");
            notificationHandler.displayMessageNotification("Warning", "Cannot send message: WebSocket is not connected");
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
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error shutting down scheduler: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
            session = null; // Reset session
            logger.info("Disconnected from WebSocket server");
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    private void scheduleReconnect() {
        synchronized (lock) {
            if (!isRunning || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                logger.info("Max reconnect attempts ({}) reached or client stopped", MAX_RECONNECT_ATTEMPTS);
                notificationHandler.displayMessageNotification("Error", "Failed to reconnect after " + MAX_RECONNECT_ATTEMPTS + " attempts");
                return;
            }
        }

        reconnectAttempts++;
        long delay = INITIAL_RECONNECT_DELAY * (long) Math.pow(2, reconnectAttempts - 1);
        logger.info("Scheduling reconnect attempt {} in {} seconds", reconnectAttempts, delay);
        scheduler.schedule(() -> {
            if (isRunning) {
                disconnect();
                connect();
            }
        }, delay, TimeUnit.SECONDS);
    }
}