package com.example.backend_chat.Notification;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class NotificationPayload {
    private NotificationType type;
    private String message;
    private String fromUserEmail;
    private String toUserEmail;
    private Object data;
}
