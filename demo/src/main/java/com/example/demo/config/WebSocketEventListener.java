package com.example.demo.config;

import com.example.demo.DTO.ChatMessage;
import com.example.demo.DTO.Enum.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
//@RequiredArgsConstructor
public class WebSocketEventListener {
    @Autowired
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener( SimpMessageSendingOperations messagingTemplate ) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            System.out.println("User disconnected: "+username);
            var chatMessage = new ChatMessage();
            chatMessage.setMessageType(MessageType.LEAVE);
            chatMessage.setSender(username);
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
