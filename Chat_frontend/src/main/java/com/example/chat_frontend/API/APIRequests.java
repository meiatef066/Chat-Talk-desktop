package com.example.chat_frontend.API;

import com.example.chat_frontend.utils.TokenManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIRequests {
    public static HttpURLConnection POSTHttpURLConnection( URL url, String userJson ) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // configure a connection
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[]input= userJson.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input);
        }
        return connection;
    }

    public static HttpURLConnection GETHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getToken());
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

}
