package com.example.backend_chat.repository;

import com.example.backend_chat.model.ChatParticipant;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository  extends JpaRepository<ChatParticipant,Long> {
    boolean existsByChatIdAndUserId( @NotNull(message = "Chat ID is required") Long chatId, Long id );
}
