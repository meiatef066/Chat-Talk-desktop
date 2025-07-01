package com.example.frontend_chat.API.AuthAPI;

import com.example.frontend_chat.DTO.LoginRequest;
import com.example.frontend_chat.DTO.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class AuthAPI {
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void signupAsync( RegisterRequest user, Consumer<String> onSuccess, Consumer<String> onError) {
        sendRequestAsync("/signup", user, onSuccess, onError);
    }

    public static void loginAsync( LoginRequest user, Consumer<String> onSuccess, Consumer<String> onError) {
        sendRequestAsync("/login", user, onSuccess, onError);
    }

    private static void sendRequestAsync(String endpoint, Object requestBody, Consumer<String> onSuccess, Consumer<String> onError) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    URL url = new URL(BASE_URL + endpoint);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json");

                    // Serialize request body to JSON
                    String json = mapper.writeValueAsString(requestBody);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCode = con.getResponseCode();
                    InputStream is = (responseCode >= 200 && responseCode < 300) ? con.getInputStream() : con.getErrorStream();

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) response.append(line);

                        JsonNode jsonNode = mapper.readTree(response.toString());

                        if (jsonNode.has("token")) {
                            String token = jsonNode.get("token").asText();
                            javafx.application.Platform.runLater(() -> onSuccess.accept(token));
                        } else {
                            String msg = jsonNode.has("message") ? jsonNode.get("message").asText() : "Unknown error";
                            javafx.application.Platform.runLater(() -> onError.accept(msg));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> onError.accept("Request failed: " + e.getMessage()));
                }
                return null;
            }
        };

        new Thread(task).start();
    }}