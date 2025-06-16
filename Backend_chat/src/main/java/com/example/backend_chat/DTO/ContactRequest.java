package com.example.backend_chat.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {
    @NotBlank
    @Email
    private String sender;
    @NotBlank
    @Email// sender
    private String receiver;    // receiver
}
