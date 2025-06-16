package com.example.chat_frontend.API;

import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class APIService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public APIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public JsonNode post( String targetUrl, Object requestBody ) throws IOException, InterruptedException {
        validateUrl(targetUrl);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .build();
        return sendRequest(request);
    }

    public JsonNode get(String targetUrl) throws IOException, InterruptedException {
        validateUrl(targetUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer "+ TokenManager.getInstance())
                .GET()

                .timeout(Duration.ofSeconds(10))
                .build();
        return sendRequest(request);
    }

    public JsonNode patch(String targetUrl, Object requestBody) throws IOException, InterruptedException {
        validateUrl(targetUrl);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization","Bearer "+ TokenManager.getInstance())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .build();
        return sendRequest(request);
    }

    private void validateUrl(String targetUrl) {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Target URL cannot be null or empty");
        }
        try {
            URI.create(targetUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL: " + targetUrl, e);
        }
    }

    private JsonNode sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            String body = response.body();
            return body.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
        } else {
            throw new IOException(String.format("Request failed with status %d: %s", statusCode, response.body()));
        }
    }
}