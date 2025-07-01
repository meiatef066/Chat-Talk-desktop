package com.example.frontend_chat.WebSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.*;
import java.util.concurrent.*;

public class GeneralWebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(GeneralWebSocketClient.class);

    private final String baseUrl;
    private final String token;
    private final boolean useSockJS;
    private final boolean enableReconnect;
    private final int maxReconnectAttempts;
    private final long reconnectDelaySeconds;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int reconnectAttempts = 0;
    private volatile boolean isRunning = false;

    private final Map<String, StompFrameHandler> subscriptions = new HashMap<>();

    public GeneralWebSocketClient(String baseUrl, String token, boolean useSockJS, boolean enableReconnect) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.useSockJS = useSockJS;
        this.enableReconnect = enableReconnect;
        this.maxReconnectAttempts = 5;
        this.reconnectDelaySeconds = 5;
    }

    public void addSubscription(String topic, StompFrameHandler handler) {
        subscriptions.put(topic, handler);
    }

    public void connect() {
        if (isRunning) {
            logger.info("WebSocket already running.");
            return;
        }
        isRunning = true;
        reconnectAttempts = 0;

        try {
            stompClient = createStompClient();
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            String fullUrl = baseUrl + "?token=" + token;
            logger.info("Connecting to {}", fullUrl);

            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("Authorization", "Bearer " + token);

            stompClient.connect(fullUrl, headers, connectHeaders, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession sess, StompHeaders connectedHeaders) {
                    session = sess;
                    logger.info("WebSocket connected successfully.");
                    reconnectAttempts = 0;
                    subscribeAll();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("Transport error: {}", exception.getMessage(), exception);
                    handleReconnect();
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    logger.error("STOMP error: {}", exception.getMessage(), exception);
                    handleReconnect();
                }
            });
        } catch (Exception e) {
            logger.error("Connection failed: {}", e.getMessage(), e);
            handleReconnect();
        }
    }

    private WebSocketStompClient createStompClient() {
        if (useSockJS) {
            return new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        }
        return new WebSocketStompClient(new StandardWebSocketClient());
    }

    private void subscribeAll() {
        subscriptions.forEach((topic, handler) -> {
            session.subscribe(topic, handler);
            logger.info("Subscribed to {}", topic);
        });
    }

    public void send(String destination, Object payload) {
        if (session != null && session.isConnected()) {
            StompHeaders headers = new StompHeaders();
            headers.setDestination(destination);
            headers.add("Authorization", "Bearer " + token);
            session.send(headers, payload);
            logger.debug("Sent to {}", destination);
        } else {
            logger.warn("Cannot send: WebSocket not connected.");
        }
    }

    public void disconnect() {
        isRunning = false;
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
        scheduler.shutdownNow();
        logger.info("Disconnected from WebSocket.");
    }

    private void handleReconnect() {
        if (!enableReconnect || reconnectAttempts >= maxReconnectAttempts) {
            logger.warn("Reconnect disabled or max attempts reached.");
            return;
        }
        reconnectAttempts++;
        long delay = reconnectDelaySeconds * reconnectAttempts;
        logger.info("Scheduling reconnect in {} seconds (Attempt {}/{})", delay, reconnectAttempts, maxReconnectAttempts);

        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }
}
