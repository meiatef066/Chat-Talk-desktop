package com.example.backend_chat.IntegrationTest;

import com.example.backend_chat.DTO.LoginRequest;
import com.example.backend_chat.DTO.RegisterRequest;
import com.example.backend_chat.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testEmail = "integration__test@gmail.com";
    private final String testPassword = "ValidPass123";
    private final String testFirstName = "Integration";
    private final String testLastName = "Test";

    private final String registerUrl = "/api/auth/signup";
    private final String loginUrl = "/api/auth/login";

    @BeforeEach
    void setUp() {
        // Clean up test user before each test
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
    }

    @AfterEach
    void tearDown() {
        // Clean up test user after each test
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
    }

    private void registerTestUser() throws Exception {
        RegisterRequest request = new RegisterRequest(testFirstName, testLastName, testEmail, testPassword);
        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(testFirstName, testLastName, testEmail, testPassword);

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegister_FailsWithDuplicateEmail() throws Exception {
        // Register user first
        registerTestUser();

        // Try registering again with the same email
        RegisterRequest request = new RegisterRequest(testFirstName, testLastName, testEmail, testPassword);
        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email already in use: " + testEmail)));
    }

    @Test
    void testRegister_FailsWithInvalidData() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "invalid-email", "");

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"))
                .andExpect(jsonPath("$.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.email").value("Email must be valid"))
                .andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));

    }

    @Test
    void testLogin_Success() throws Exception {
        // Register user
        registerTestUser();

        // Perform login
        LoginRequest request = new LoginRequest(testEmail, testPassword);
        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_FailsWithNonexistentUser() throws Exception {
        LoginRequest request = new LoginRequest("nouser@example.com", "irrelevant");

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void testLogin_FailsWithWrongPassword() throws Exception {
        // Register user
        registerTestUser();

        // Perform login with wrong password
        LoginRequest request = new LoginRequest(testEmail, "wrongpassword");
        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid email or password")));
    }

    @Test
    void testLogin_FailsWithInvalidData() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email is required"))
                .andExpect(jsonPath("$.password").value("Password is required"));
    }
}