package com.example.chat_frontend.utils;

public class TokenManager {
    private static TokenManager instance ;
    private String token;
    private TokenManager() {}
    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
    public void clearToken() {
        this.token = null;
    }
    public boolean isAuthenticated() {
        return token != null;
    }
}
