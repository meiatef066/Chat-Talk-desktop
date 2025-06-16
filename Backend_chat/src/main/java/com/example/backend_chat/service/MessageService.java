package com.example.backend_chat.service;

import com.example.backend_chat.DTO.MessageDTO;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.model.Message;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ChatParticipantRepository;
import com.example.backend_chat.repository.ChatRepository;
import com.example.backend_chat.repository.MessageRepository;
import com.example.backend_chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageService( MessageRepository messageRepository, UserRepository userRepository, ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository, SimpMessagingTemplate simpMessagingTemplate ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public Message sendMessage( MessageDTO messageDTO ) {
        Chat chat = chatRepository.findById(messageDTO.getChatId()).orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        User sender = userRepository.findByEmail(messageDTO.getSenderEmail()).orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Message message = Message.builder().chat(chat).sender(sender).content(messageDTO.getContent()).messageType(messageDTO.getMessageType()).isRead(false).build();
        Message savedMessage = messageRepository.save(message);

        // update last message in chat
        chat.setLastMessage(savedMessage);
        chatRepository.save(chat);

        // 🔥 Broadcast to everyone subscribed to the chat room
        // send message to the client
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), message);

        return savedMessage;

    }

    public List<Message> getMessagesForChat( Long chatId ) {
        return messageRepository.findByChatIdOrderBySentAtAsc(chatId);
    }

}