package com.example.backend_chat.service;

import com.example.backend_chat.DTO.CreateChatRequest;
import com.example.backend_chat.model.Chat;
import com.example.backend_chat.model.ChatParticipant;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ChatRepository;
import com.example.backend_chat.repository.ChatParticipantRepository;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository participantRepository;

    public ChatService( ChatRepository chatRepository, UserRepository userRepository, ChatParticipantRepository participantRepository ) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.chatRepository = chatRepository;
    }

    public Chat createChat( CreateChatRequest request ) {
        Chat chat = Chat.builder().chatName(request.isGroup() ? request.getChatName() : null).isGroup(request.isGroup()).build();
        chat = chatRepository.save(chat);

        for (String email : request.getParticipantEmails()) {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            ChatParticipant participant = ChatParticipant.builder().chat(chat).user(user).build();
            participantRepository.save(participant);
        }
        return chat;
    }
}
