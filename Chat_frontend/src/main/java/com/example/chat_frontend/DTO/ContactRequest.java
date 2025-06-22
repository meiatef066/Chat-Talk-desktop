package com.example.chat_frontend.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class ContactRequest {
    private String sender;
    private String receiver;

    public ContactRequest(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
}