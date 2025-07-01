package com.example.frontend_chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequest {
    private String sender;
    private String receiver;    // receiver
}
