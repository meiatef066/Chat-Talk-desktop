package com.example.backend_chat.DTO;

import com.example.backend_chat.model.ENUM.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageType messageType;
    private boolean isRead;
    private LocalDateTime sentAt;
}