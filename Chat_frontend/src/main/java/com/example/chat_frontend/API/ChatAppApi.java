//package com.example.chat_frontend.API;
//
//import com.example.chat_frontend.DTO.ContactResponse;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChatAppApi {
//    private final ApiClient apiClient;
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//    public ChatAppApi( ApiClient apiClient ) {
//        this.apiClient = apiClient;
//    }
//
//    public JsonNode getAllAcceptedUser() throws IOException, InterruptedException {
//        JsonNode response = apiClient.get("/contacts/request/accepted",null);
//        System.out.println(response);
//        return response;
//    }
//    public JsonNode getPendingRequests() throws IOException, InterruptedException {
//        JsonNode response = apiClient.get("/contacts/request/pending",null);
//        System.out.println(response);
//        return response;
//    }
//    public String responseToPendingRequest(String url) throws IOException, InterruptedException {
//        JsonNode response = apiClient.patch(url,null);
//        return response.toString();
//    }
//
//}


package com.example.chat_frontend.API;

import com.example.chat_frontend.DTO.ApiResponse;
import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ChatAppApi {
    private final String baseUrl = "http://localhost:8080/api/contacts/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatAppApi() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode getAllAcceptedUser() throws IOException, InterruptedException {
        return sendGetRequest("request/accepted");
    }

    public JsonNode getPendingRequests() throws IOException, InterruptedException {
        return sendGetRequest("request/pending");
    }

    public ApiResponse respondToPendingRequest( String senderEmail, String action) throws IOException, InterruptedException {
        String encodedEmail = URLEncoder.encode(senderEmail, StandardCharsets.UTF_8);
        String url = senderEmail + "/response?action=" + action;
        JsonNode response = sendPatchRequest(url);
        return objectMapper.treeToValue(response, ApiResponse.class);
    }

    private JsonNode sendGetRequest(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Authorization", "Bearer " + TokenManager.getInstance().getToken())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private JsonNode sendPatchRequest(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + TokenManager.getInstance().getToken())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }
}