package com.example.backend_chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {
    private Long id;
    private SimpleUserDTO user;
    private SimpleUserDTO contact;
    private String status;
//    private LocalDateTime createdAt;
}
