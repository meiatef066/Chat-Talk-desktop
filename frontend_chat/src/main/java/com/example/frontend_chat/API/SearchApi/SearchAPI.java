package com.example.frontend_chat.API.SearchApi;

import com.example.frontend_chat.DTO.ContactRequest;
import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.utils.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SearchAPI {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper mapper = new ObjectMapper();
    public SearchAPI() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = TokenManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            return execution.execute(request, body);
        });
    }


    public ContactResponse addContactRequest(String email){
       ContactRequest contactRequest = new ContactRequest();
       contactRequest.setReceiver(email);
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TokenManager.getInstance().getToken());

        // Combine body and headers
        HttpEntity<ContactRequest> entity = new HttpEntity<>(contactRequest, headers);

        // Make POST request
        ResponseEntity<ContactResponse> response = restTemplate.exchange(
                BASE_URL + "/contacts/request",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ContactResponse>() {}
        );

        return response.getBody();
   }
    // SearchAPI.java
    public List<ContactResponse> searchUsers(String query) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8); // Safer encoding
        String url = BASE_URL + "/users/search?query=" + encodedQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TokenManager.getInstance().getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ContactResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ContactResponse>>() {}
        );

        return response.getBody();
    }

}
