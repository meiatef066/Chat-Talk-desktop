package com.example.backend_chat.DTO;

import com.example.backend_chat.model.ENUM.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Data
@Builder
public class MessageDTO {
    @NotNull(message = "Chat ID is required")
    private Long chatId;

    @NotBlank(message = "Sender email is required")
    private String senderEmail;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private MessageType messageType;
    private LocalDateTime sentAt;


}
