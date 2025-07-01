package com.example.frontend_chat.utils;

import lombok.Getter;

@Getter
public class TokenManager {
    private static TokenManager instance ;
    private String token;
    private String email;
    private TokenManager() {}
    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void setToken(String token, String email) {
        this.token = token;
        this.email = email;
    }

    public void clearToken() {
        this.token = null;
    }
    public boolean isAuthenticated() {
        return token != null;
    }

}
