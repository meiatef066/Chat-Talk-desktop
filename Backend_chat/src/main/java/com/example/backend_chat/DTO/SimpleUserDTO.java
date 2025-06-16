package com.example.backend_chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleUserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private Boolean isOnline;
}
