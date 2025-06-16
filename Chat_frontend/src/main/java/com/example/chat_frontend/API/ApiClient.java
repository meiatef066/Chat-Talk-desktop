package com.example.chat_frontend.API;

import com.example.chat_frontend.utils.TokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

public class ApiClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Set<String> authExemptEndpoints;
    @Getter
    private final TokenManager tokenManager;

    public ApiClient() {
        this(System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "http://localhost:8080/api",
                Set.of("/auth/login", "/auth/signup"),
                TokenManager.getInstance());
    }

    public ApiClient(String baseUrl, Set<String> authExemptEndpoints, TokenManager tokenManager) {
        this.baseUrl = baseUrl;
        this.authExemptEndpoints = authExemptEndpoints;
        this.tokenManager = tokenManager;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public JsonNode get(String endpoint,Object body) throws IOException, InterruptedException {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
        return sendRequest(endpoint, "GET", jsonBody);
    }

    public JsonNode post(String endpoint, Object body) throws IOException, InterruptedException {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
        return sendRequest(endpoint, "POST", jsonBody);
    }

    public JsonNode patch(String endpoint, Object body) throws IOException, InterruptedException {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
        return sendRequest(endpoint, "PATCH", jsonBody);
    }

    private JsonNode sendRequest(String endpoint, String method, String body) throws IOException, InterruptedException {
        String targetUrl = baseUrl + endpoint;
        validateUrl(targetUrl);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10));

        if (body != null && (method.equals("POST") || method.equals("PATCH"))) {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        if (!authExemptEndpoints.contains(endpoint)) {
            String token = tokenManager.getToken();
            if (token == null || token.isEmpty()) {
                throw new UnauthorizedException("No authentication token available");
            }
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int statusCode = response.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = response.body();
                    return responseBody.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(responseBody);
                } else {
                    throw mapHttpError(statusCode, response.body());
                }
            } catch (IOException e) {
                if (++retryCount == maxRetries) throw new IOException("Request failed after " + maxRetries + " attempts", e);
                Thread.sleep(1000 * retryCount);
            }
        }
        throw new IOException("Request failed after " + maxRetries + " attempts");
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

    IOException mapHttpError(int statusCode, String body) {
        try {
            JsonNode error = objectMapper.readTree(body);
            String message = error.has("message") ? error.get("message").asText() : body;
            return switch (statusCode) {
                case 401 -> new UnauthorizedException("Unauthorized: " + message);
                case 403 -> new IOException("Forbidden: " + message);
                case 404 -> new IOException("Resource not found: " + message);
                default -> new IOException(String.format("Request failed with status %d: %s", statusCode, message));
            };
        } catch (IOException e) {
            return new IOException(String.format("Request failed with status %d: %s", statusCode, body));
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public URI getUri(String endpoint) {
        return URI.create(baseUrl + endpoint);
    }

    public static class UnauthorizedException extends IOException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}