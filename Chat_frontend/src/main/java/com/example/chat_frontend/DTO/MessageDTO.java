package com.example.chat_frontend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    @JsonProperty("chatId")
    private Long chatId;

    @JsonProperty("senderEmail")
    private String senderEmail;

    @JsonProperty("content")
    private String content;

    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("sentAt")
    private LocalDateTime sentAt;

    @JsonProperty("isRead")
    private boolean isRead;

    public MessageDTO( Long chatId, String email, String text, String string, boolean b ) {
        this.chatId = chatId;
        this.senderEmail = email;
        this.content = text;
        this.messageType = string;
        this.isRead = b;
    }


}
