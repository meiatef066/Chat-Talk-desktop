package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.CreateChatRequest;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<Chat> CreateChat( @RequestBody CreateChatRequest request )
    {
        Chat chat =chatService.createChat(request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/private")
    public ResponseEntity<Chat> getOrCreatePrivateChat( @RequestParam String user1) {
        String user2= SecurityContextHolder.getContext().getAuthentication().getName();
        Chat chat = chatService.getOrCreatePrivateChat(user1, user2);
        return ResponseEntity.ok(chat);
    }

}
