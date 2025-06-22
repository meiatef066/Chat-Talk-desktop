package com.example.demo.DTO;

import com.example.demo.DTO.Enum.MessageType;
import lombok.*;



@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String message;
    private MessageType messageType;

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType( MessageType messageType ) {
        this.messageType = messageType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender( String sender ) {
        this.sender = sender;
    }
}
