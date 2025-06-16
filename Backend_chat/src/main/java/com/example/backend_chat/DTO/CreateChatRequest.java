package com.example.backend_chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRequest {
    private String chatName;
    private boolean isGroup;
    private Set<String> participantEmails; // 🟢 use emails instead of IDs
}