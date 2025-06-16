package com.example.chat_frontend.API;

import com.example.chat_frontend.Model.User;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class AuthApi {
    private final ApiClient apiClient;

    public AuthApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Logs in a user and returns the JWT token.
     * @param user User object with email and password.
     * @return JWT token from the response.
     * @throws IOException If the request fails (e.g., network error).
     * @throws InterruptedException If the request is interrupted.
     * @throws IllegalStateException If the response lacks a token.
     */
    public String login(User user) throws IOException, InterruptedException {
        JsonNode response = apiClient.post("/auth/login", user);
        if (!response.has("token")) {
            throw new IllegalStateException("Login response does not contain a token");
        }
        System.out.println(response.get("token").asText());
        return response.get("token").asText();
    }

    /**
     * Signs up a user and returns the JWT token.
     * @param user User object with firstName, lastName, email, and password.
     * @return JWT token from the response.
     * @throws IOException If the request fails (e.g., network error).
     * @throws InterruptedException If the request is interrupted.
     * @throws IllegalStateException If the response lacks a token.
     */
    public String signup(User user) throws IOException, InterruptedException {
        JsonNode response = apiClient.post("/auth/signup", user);
        if (!response.has("token")) {
            throw new IllegalStateException("Signup response does not contain a token");
        }
        return response.get("token").asText();
    }
}