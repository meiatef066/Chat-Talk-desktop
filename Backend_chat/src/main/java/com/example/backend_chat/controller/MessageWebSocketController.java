package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.MessageDTO;
import com.example.backend_chat.model.Message;
import com.example.backend_chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public MessageDTO sendMessage( @Valid @Payload MessageDTO messageDTO, Principal principal) {
//        log.info("Received global message from: {}", principal.getName());
//        if (!principal.getName().equals(messageDTO.getSenderEmail())) {
//            throw new SecurityException("Unauthorized sender");
//        }
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String userEmail = auth.getName();
//        if (!userEmail.equals(messageDTO.getSenderEmail())) {
//            log.error("Authenticated user {} does not match sender email {}", userEmail, messageDTO.getSenderEmail());
//            throw new SecurityException("Unauthorized sender");
//        }
        Message message= messageService.sendMessage(messageDTO);
        MessageDTO messageDTOResponse = new MessageDTO();
        messageDTOResponse.setChatId(message.getChat().getId());
        messageDTOResponse.setContent(message.getContent());
        return messageDTOResponse;
    }

    // REST Endpoint: Fetch all messages of a chat
    @GetMapping("/api/messages/{chatId}")
    public List<Message> getMessages( @PathVariable Long chatId) {
        return messageService.getMessagesForChat(chatId);
    }
    @MessageMapping("/chat")
    public void handleChatMessage( @Payload MessageDTO message, Principal principal) {
        System.out.println("Received message from: " + principal.getName());
        // now you can trust the user is authenticated
    }
}