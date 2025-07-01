package com.example.chat_frontend.Model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Chat {
    private Long id;

    private String chatName;
    private boolean isGroup;
    private LocalDateTime createdAt;
    private Set<ChatParticipant> participants = new HashSet<>();
    private Message lastMessage;
}
