package com.example.frontend_chat.DTO;

import com.example.frontend_chat.DTO.Enum.NotificationType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class NotificationPayload {
    private NotificationType type;
    private String message;
    private String fromUserEmail;
    private String toUserEmail;
    private Object data;
}
