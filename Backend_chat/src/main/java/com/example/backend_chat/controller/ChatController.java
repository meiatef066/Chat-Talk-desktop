package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.CreateChatRequest;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
