package com.example.chat_frontend.API;

import com.example.chat_frontend.DTO.UpdateUserProfileRequest;
import com.example.chat_frontend.Model.User;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ProfileApi {
    private final ApiClient apiClient;
    private static final String PROFILE_ENDPOINT = "/profile";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_MIME_TYPES = {"image/png", "image/jpeg", "image/jpg"};

    public ProfileApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public User getProfile() throws IOException, InterruptedException {
        JsonNode response = apiClient.get(PROFILE_ENDPOINT,null);
        return apiClient.getObjectMapper().treeToValue(response, User.class);
    }

    public User updateProfile(UpdateUserProfileRequest request) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
        ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();

        // Add text fields as multipart parts
        appendTextPart(bodyStream, boundary, "firstName", request.getFirstName());
        appendTextPart(bodyStream, boundary, "lastName", request.getLastName());
        appendTextPart(bodyStream, boundary, "email", request.getEmail());
        appendTextPart(bodyStream, boundary, "bio", request.getBio());
        appendTextPart(bodyStream, boundary, "gender", request.getGender());
        appendTextPart(bodyStream, boundary, "phoneNumber", request.getPhoneNumber());
        appendTextPart(bodyStream, boundary, "country", request.getCountry());

        // Add profile picture file if present
        File profilePicture = request.getProfilePicture();
        if (profilePicture != null && profilePicture.exists()) {
            // Validate file
            String mimeType = Files.probeContentType(profilePicture.toPath());
            if (mimeType == null || !isAllowedMimeType(mimeType)) {
                throw new IllegalArgumentException("Invalid image file type. Allowed types: PNG, JPG, JPEG");
            }
            if (profilePicture.length() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Image file size exceeds 5MB limit");
            }

            // Add file part
            bodyStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write(("Content-Disposition: form-data; name=\"profilePicture\"; filename=\"" +
                    profilePicture.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write(("Content-Type: " + mimeType + "\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            bodyStream.write(Files.readAllBytes(profilePicture.toPath()));
            bodyStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        // Close multipart body
        bodyStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        // Build HTTP request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(apiClient.getUri(PROFILE_ENDPOINT))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiClient.getTokenManager().getToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofByteArray(bodyStream.toByteArray()))
                .timeout(Duration.ofSeconds(10))
                .build();

        // Send request
        HttpResponse<String> response = apiClient.getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            String responseBody = response.body();
            JsonNode jsonNode = responseBody.isEmpty() ? apiClient.getObjectMapper().createObjectNode() :
                    apiClient.getObjectMapper().readTree(responseBody);
            return apiClient.getObjectMapper().treeToValue(jsonNode, User.class);
        } else {
            throw apiClient.mapHttpError(statusCode, response.body());
        }
    }

    private void appendTextPart( ByteArrayOutputStream bodyStream, String boundary, String name, String value) throws IOException {
        if (value != null && !value.trim().isEmpty()) {
            bodyStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write(value.getBytes(StandardCharsets.UTF_8));
            bodyStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean isAllowedMimeType(String mimeType) {
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
}