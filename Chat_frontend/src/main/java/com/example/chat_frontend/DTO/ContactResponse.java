package com.example.chat_frontend.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class ContactResponse {
    private Long id;
    private SimpleUserDTO user;
    private SimpleUserDTO contact;
    private String status;
//    private LocalDateTime createdAt;
}