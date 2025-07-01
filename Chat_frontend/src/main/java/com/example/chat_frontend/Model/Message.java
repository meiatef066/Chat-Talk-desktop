package com.example.chat_frontend.Model;

import com.example.chat_frontend.DTO.eum.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Message {
    private Long id;
    private Chat chat;
    private User sender;
    private String content;
    private MessageType messageType;
    private LocalDateTime sentAt ;
    private boolean isRead = false;

}
