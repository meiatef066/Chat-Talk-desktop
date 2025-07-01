package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.MessageDTO;
import com.example.backend_chat.model.Message;
import com.example.backend_chat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO chatMessageDTO) {
        messageService.saveMessage(chatMessageDTO);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<Message>> getChatMessages(@PathVariable Long chatId) {
        List<Message> messages = messageService.getMessagesForChat(chatId);
        return ResponseEntity.ok(messages);
    }
}