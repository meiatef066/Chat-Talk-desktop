package com.example.frontend_chat.API.MainPageAPI;

import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.utils.TokenManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ChatAppApi {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080/api/contacts";

    public ChatAppApi() {
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


    public ResponseEntity<List<ContactResponse>> getAcceptedContacts() {
        return restTemplate.exchange(
                BASE_URL + "/request/accepted",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ContactResponse>>() {}
        );
    }

    public ResponseEntity<List<ContactResponse>> getPendingContacts() {
        return restTemplate.exchange(
                BASE_URL + "/request/pending",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ContactResponse>>() {}
        );
    }

    public ResponseEntity<ContactResponse> respondToContactRequest(String senderEmail, String action) {
        return restTemplate.exchange(
                BASE_URL + "/" + senderEmail + "/response?action=" + action,
                HttpMethod.PATCH,
                null,
                ContactResponse.class
        );
    }

    public ResponseEntity<String> acceptContactRequest(String senderEmail) {
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                BASE_URL+"/"+senderEmail,
                HttpMethod.PATCH,
                null,
                String.class
        );
    }
    public ResponseEntity<String> rejectgContactRequest(String senderEmail) {
        return restTemplate.exchange(
                BASE_URL+"/"+senderEmail,
                HttpMethod.DELETE,
                null,
                String.class
        );
    }
}