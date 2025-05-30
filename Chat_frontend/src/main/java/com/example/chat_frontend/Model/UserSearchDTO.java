package com.example.chat_frontend.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSearchDTO {
    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;

    // Default no-args constructor required by Jackson
    public UserSearchDTO() {
    }

    // Parameterized constructor
    public UserSearchDTO(@JsonProperty("id") Long id,
                         @JsonProperty("email") String email,
                         @JsonProperty("displayName") String displayName,
                         @JsonProperty("avatarUrl") String avatarUrl) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', displayName='" + displayName + "', avatarUrl='" + avatarUrl + "'}";
    }
}