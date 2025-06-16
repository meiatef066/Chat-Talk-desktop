package com.example.backend_chat.DTO;

import lombok.Getter;

@Getter
public class UserSearchDTO {
    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    // Constructors
    public UserSearchDTO( Long id, String email, String displayName, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
}
