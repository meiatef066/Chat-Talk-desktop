package com.example.chat_frontend.API;

import com.example.chat_frontend.DTO.ContactResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatAppApi {
    private final ApiClient apiClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public ChatAppApi( ApiClient apiClient ) {
        this.apiClient = apiClient;
    }

    public JsonNode getAllAcceptedUser() throws IOException, InterruptedException {
        JsonNode response = apiClient.get("/contacts/accepted",null);
        System.out.println(response);
        return response;
    }
    public JsonNode getPendingRequests() throws IOException, InterruptedException {
        JsonNode response = apiClient.get("/contacts/pending",null);
        System.out.println(response);
        return response;
    }
    public String responseToPendingRequest(String url) throws IOException, InterruptedException {
        JsonNode response = apiClient.patch(url,null);
        return response.toString();
    }

}
