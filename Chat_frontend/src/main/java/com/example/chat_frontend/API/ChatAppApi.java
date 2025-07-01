package com.example.chat_frontend.API;

import com.example.chat_frontend.DTO.ContactResponse;
import com.example.chat_frontend.DTO.MessageDTO;
import com.example.chat_frontend.Model.Chat;
import com.example.chat_frontend.controller.ChatApp;
import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatAppApi {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatApp chatApp;

    public ChatAppApi(ChatApp chatApp) {
        this.baseUrl = System.getenv("CHAT_API_URL") != null
                ? System.getenv("CHAT_API_URL")
                : "http://localhost:8080/api/";
        this.httpClient = HttpClient.newHttpClient();
        this.chatApp = chatApp;
    }

    public JsonNode getAllAcceptedUser() throws ChatApiException {
        return sendGetRequest("contacts/request/accepted");
    }

    public JsonNode getPendingRequests() throws ChatApiException {
        return sendGetRequest("contacts/request/pending");
    }

    public ContactResponse respondToPendingRequest(String senderEmail, String action) throws ChatApiException {
        String encodedEmail = URLEncoder.encode(senderEmail, StandardCharsets.UTF_8);
        String url = "contacts/" + encodedEmail + "/response?action=" + action;
        JsonNode response = sendPatchRequest(url);
        try {
            return objectMapper.treeToValue(response, ContactResponse.class);
        } catch (IOException e) {
            throw new ChatApiException("Failed to parse contact response for " + senderEmail, e);
        }
    }

    public List<MessageDTO> getChatMessages(Long chatId) throws ChatApiException {
        JsonNode node = sendGetRequest("chats/" + chatId + "/messages");
        try {
            return objectMapper.convertValue(node, new TypeReference<List<MessageDTO>>() {});
        } catch (IllegalArgumentException e) {
            throw new ChatApiException("Failed to parse messages for chat " + chatId, e);
        }
    }

    public JsonNode searchUsers(String query) throws ChatApiException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return sendGetRequest("users/search?query=" + encodedQuery);
    }

    public Chat getOrCreatePrivateChat(String user1, String user2) throws ChatApiException {
        String encodedUser1 = URLEncoder.encode(user1, StandardCharsets.UTF_8);
        String encodedUser2 = URLEncoder.encode(user2, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "chats/private?user1=" + encodedUser1 + "&user2=" + encodedUser2))
                .header("Authorization", "Bearer " + TokenManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Chat.class);
            } else {
                throw new ChatApiException("Failed to fetch/create private chat: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new ChatApiException("Error creating private chat between " + user1 + " and " + user2, e);
        }
    }

    public void acceptFriendRequest(String senderEmail) {
        try {
            ContactResponse response = respondToPendingRequest(senderEmail, "ACCEPTED");
            chatApp.getFriends().add(response.getContact());
        } catch (ChatApiException e) {
            throw new RuntimeException("Failed to accept friend request: " + e.getMessage(), e);
        }
    }

    public void rejectFriendRequest(String senderEmail) {
        try {
            respondToPendingRequest(senderEmail, "REJECTED");
        } catch (ChatApiException e) {
            throw new RuntimeException("Failed to reject friend request: " + e.getMessage(), e);
        }
    }

    private JsonNode sendGetRequest(String endpoint) throws ChatApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Authorization", "Bearer " + TokenManager.getInstance().getToken())
                .build();

    }

    private JsonNode sendPatchRequest(String endpoint)  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + TokenManager.getInstance().getToken())
                .build();
    }
}
