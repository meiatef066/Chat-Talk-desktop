package com.example.backend_chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService( SimpMessagingTemplate messagingTemplate ) {
        this.messagingTemplate = messagingTemplate;
    }
    public void notifyUser(String email, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(email, destination, payload);
        System.out.println("🔔 WebSocket sent to " + email + " -> " + destination);
    }

}
