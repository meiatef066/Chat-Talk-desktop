package com.example.backend_chat.Notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(String userEmail, NotificationPayload payload) {
        String destination = getDestinationForType(payload.getType());
        messagingTemplate.convertAndSendToUser(userEmail,destination, payload);
        System.out.println("🔔 Sent [" + payload.getType() + "] to " + userEmail);
    }

    private String getDestinationForType(NotificationType type) {
        return switch (type) {
            case FRIEND_REQUEST -> "/queue/friend-request";
            case FRIEND_REQUEST_ACCEPTED, FRIEND_REQUEST_REJECTED -> "/queue/friend-request-response";
            case MESSAGE -> "/queue/messages";
            case ERROR -> "/queue/errors";
            case SYSTEM -> "/queue/system";
        };
    }
}