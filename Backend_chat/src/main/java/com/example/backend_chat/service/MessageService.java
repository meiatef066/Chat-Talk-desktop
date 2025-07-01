package com.example.backend_chat.service;

import com.example.backend_chat.DTO.MessageDTO;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.model.ENUM.MessageType;
import com.example.backend_chat.model.Message;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ChatRepository;
import com.example.backend_chat.repository.MessageRepository;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository,
                          ChatRepository chatRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void saveMessage( MessageDTO dto) {
        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + dto.getChatId()));
        User sender = userRepository.findByEmail(dto.getSenderEmail())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + dto.getSenderEmail()));

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(dto.getContent())
                .messageType(dto.getMessageType() != null ? MessageType.valueOf(String.valueOf(dto.getMessageType())) : MessageType.TEXT)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        chat.setLastMessage(savedMessage);
        chatRepository.save(chat);
        // Create a DTO to send (you may include timestamp, chatId, etc.)
        MessageDTO outgoingDto = MessageDTO.builder()
                .chatId(chat.getId())
                .senderEmail(sender.getEmail())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt()) // add this field if needed
                .build();
        // Send to all participants except the sender
        chat.getParticipants().forEach(participant -> {
            User receiver = participant.getUser();
            if (!receiver.getEmail().equalsIgnoreCase(sender.getEmail())) {
                simpMessagingTemplate.convertAndSendToUser(
                        receiver.getEmail(),
                        "/topic/messages",
                        outgoingDto
                );
            }
        });
//        simpMessagingTemplate.convertAndSend("/topic/messages", dto);

    }

    public List<Message> getMessagesForChat( Long chatId) {
        return messageRepository.findByChatIdOrderBySentAtAsc(chatId);
    }
}