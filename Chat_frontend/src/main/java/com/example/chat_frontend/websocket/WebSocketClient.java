package com.example.chat_frontend.websocket;


import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.controller.ChatApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class WebSocketClient {
    private final ChatApp chatAppController;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StompSession stompSession;

    public WebSocketClient(ChatApp chatAppController, String wsUrl, String userEmail) {
        this.chatAppController = chatAppController;
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;
                session.subscribe("/user/" + userEmail + "/topic/friend-request-response", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        try {
                            ContactResponse response = objectMapper.readValue((byte[]) payload, ContactResponse.class);
                            Platform.runLater(() -> {
                                if ("ACCEPTED".equalsIgnoreCase(response.getStatus())) {
                                    chatAppController.getFriends().add(response.getContact());
                                    chatAppController.getPendingRequests().remove(response);
                                } else if ("REJECTED".equalsIgnoreCase(response.getStatus())) {
                                    chatAppController.getPendingRequests().remove(response);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
    }
}