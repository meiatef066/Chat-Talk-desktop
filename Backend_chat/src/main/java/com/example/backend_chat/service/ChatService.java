package com.example.backend_chat.service;

import com.example.backend_chat.DTO.CreateChatRequest;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.model.ChatParticipant;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ChatRepository;
import com.example.backend_chat.repository.ChatParticipantRepository;
import com.example.backend_chat.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository participantRepository;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository, ChatParticipantRepository participantRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    public Chat getOrCreatePrivateChat(String user1Email, String user2Email) {
        if (user1Email == null || user2Email == null || user1Email.equals(user2Email)) {
            throw new IllegalArgumentException("Invalid user emails for chat creation");
        }

        return chatRepository.findPrivateChatBetweenUsers(user1Email, user2Email)
                .orElseGet(() -> {
                    CreateChatRequest request = new CreateChatRequest();
                    request.setGroup(false);
                    request.setChatName(null);
                    request.setParticipantEmails(Set.of(user1Email, user2Email));
                    return createChat(request);
                });
    }

    @Transactional
    public Chat createChat(CreateChatRequest request) {
        Chat chat = Chat.builder()
                .chatName(request.isGroup() ? request.getChatName() : null)
                .isGroup(request.isGroup())
                .build();

        Set<ChatParticipant> participants = new HashSet<>();


        for (String email : request.getParticipantEmails()) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            ChatParticipant participant = ChatParticipant.builder()
                    .chat(chat)
                    .user(user)
                    .build();

            participants.add(participant);
        }
        chat.setParticipants(participants);
        return chatRepository.save(chat);
    }
}