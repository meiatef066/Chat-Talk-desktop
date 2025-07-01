package com.example.chat_frontend.Model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatParticipant {
    private Long id;
    private Chat chat;
   private User user;
   private LocalDateTime joinedAt ;
}
