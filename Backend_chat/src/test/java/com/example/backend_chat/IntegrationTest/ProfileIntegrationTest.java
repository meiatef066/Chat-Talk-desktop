package com.example.backend_chat.IntegrationTest;
import com.example.backend_chat.DTO.LoginRequest;
import com.example.backend_chat.DTO.RegisterRequest;
import com.example.backend_chat.DTO.UpdateUserProfileRequest;
import com.example.backend_chat.repository.UserRepository;
import com.example.backend_chat.utils.JwtUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@WebMvcTest(ProfileIntegrationTest.class)
public class ProfileIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private com.cloudinary.Cloudinary cloudinary;

    private final String testEmail = "profile_test@gmail.com";
    private final String testPassword = "ValidPass123";
    private final String testFirstName = "Profile";
    private final String testLastName = "Test";
    private final String profileUrl = "/api/profile";
    private String jwtToken;
    private final String registerUrl = "/api/auth/signup";
    private final String loginUrl = "/api/auth/login";

    @BeforeEach
    void setUp() throws Exception {
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
        RegisterRequest registerRequest = new RegisterRequest(testFirstName, testLastName,testEmail, testPassword);

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest(testEmail, testPassword);
        String loginResponse = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(loginResponse).get("token").asText();
    }
    @AfterEach
    void tearDown() {
        jwtToken=null;
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
    }
    @Test
    void testGetProfile_Success() throws Exception {
        mockMvc.perform(get(profileUrl)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value(testFirstName))
                .andExpect(jsonPath("$.lastName").value(testLastName));
    }
    @Test
    void testGetProfile_Unauthenticated() throws Exception {
        mockMvc.perform(get(profileUrl))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void testUpdateProfile_Success_WithoutPicture() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Updated");
        request.setBio("New bio");
        request.setCountry("USA");

        mockMvc.perform(multipart(profileUrl)
                        .param("firstName", request.getFirstName())
                        .param("bio", request.getBio())
                        .param("country", request.getCountry())
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.bio").value("New bio"))
                .andExpect(jsonPath("$.country").value("USA"));
    }
//    @Test
//    void testUpdateProfile_Success_WithPicture() throws Exception {
//        MockMultipartFile profilePicture = new MockMultipartFile(
//                "profilePicture", "test.jpg", "image/jpeg", "test image content".getBytes());
//
//        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
//        request.setFirstName("Updated");
//        request.setProfilePicture(profilePicture);
//
//        mockMvc.perform(multipart(profileUrl)
//                        .file(profilePicture)
//                        .param("firstName", request.getFirstName())
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .with(req -> {
//                            req.setMethod("PATCH");
//                            return req;
//                        }))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("Updated"))
//                .andExpect(jsonPath("$.profilePicture").value("https://cloudinary.com/test.jpg"));
//    }
}
